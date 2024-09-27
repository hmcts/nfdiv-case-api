package uk.gov.hmcts.divorce.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseMatch;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.ES_DATE_FORMATTER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.REFERENCE_KEY;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerFindMatches implements CCDConfig<CaseData, State, UserRole> {

    public static final String FIND_MATCHES = "caseworker-find-matches";
    private final CcdSearchService ccdSearchService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    protected final ObjectMapper objectMapper;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(FIND_MATCHES)
            .forStates(POST_SUBMISSION_STATES)
            .name("Find matches")
            .description("Find matches")
            .aboutToStartCallback(this::aboutToStart)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, CASE_WORKER_BULK_SCAN, SUPER_USER))
            .page("findmatch")
            .pageLabel("Search for matching cases which have same marriage date and full names")
            .readonlyNoSummary(CaseData::getCaseMatches)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start findmatches callback invoked for Case Id: {}", FIND_MATCHES, details.getId());
        CaseData caseData = details.getData();
        MarriageDetails marriageDetails = caseData.getApplication().getMarriageDetails();

        List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseMatchDetails = getFreshMatches(details, marriageDetails);
        log.info("Case ID: " + details.getId() + " case matching search result: " + caseMatchDetails.size());

        List<CaseMatch> newMatches = transformToMatchingCasesList(caseMatchDetails);
        addMatches(caseData, newMatches);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }

    List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> getFreshMatches(CaseDetails<CaseData, State> details,
                                                                                   MarriageDetails marriageDetails) {
        BoolQueryBuilder nameMatchQuery1 = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery("data.marriageApplicant1Name.keyword", marriageDetails.getApplicant1Name()))
            .filter(QueryBuilders.termQuery("data.marriageApplicant2Name.keyword", marriageDetails.getApplicant2Name()));

        BoolQueryBuilder nameMatchQuery2 = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery("data.marriageApplicant1Name.keyword", marriageDetails.getApplicant2Name()))
            .filter(QueryBuilders.termQuery("data.marriageApplicant2Name.keyword", marriageDetails.getApplicant1Name()));

        BoolQueryBuilder nameMatching = QueryBuilders.boolQuery()
            .should(nameMatchQuery1)
            .should(nameMatchQuery2);

        LocalDate marriageDate = marriageDetails.getDate();

        List<String> stateValues = POST_SUBMISSION_STATES.stream().map(State::name).toList();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termsQuery(STATE_KEY,stateValues))
            .mustNot(QueryBuilders.termQuery(REFERENCE_KEY, String.valueOf(details.getId())))
            .filter(QueryBuilders.termQuery("data.marriageDate", marriageDate.format(ES_DATE_FORMATTER)))
            .filter(nameMatching);

        final var user = idamService.retrieveSystemUpdateUserDetails();
        final var serviceAuth = authTokenGenerator.generate();
        return ccdSearchService.searchForAllCasesWithQuery(boolQuery, user, serviceAuth);
    }

    public List<CaseMatch> transformToMatchingCasesList(
        List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseMatchDetails) {

        return caseMatchDetails.stream().map(caseDetail -> {
            CaseData caseData = getCaseData(caseDetail.getData());
            Application application = caseData.getApplication();
            MarriageDetails marriageDetails = application.getMarriageDetails();

            return CaseMatch.builder()
                .applicant1Name(marriageDetails.getApplicant1Name())
                .applicant2Name(marriageDetails.getApplicant2Name())
                .date(marriageDetails.getDate())
                .applicant1Postcode(
                    caseData.getApplicant1().getAddress() != null && caseData.getApplicant1().getAddress().getPostCode() != null
                        ? caseData.getApplicant1().getAddress().getPostCode() : null)
                .applicant2Postcode(
                    caseData.getApplicant2().getAddress() != null && caseData.getApplicant2().getAddress().getPostCode() != null
                    ? caseData.getApplicant2().getAddress().getPostCode() : null)
                .applicant1Town(
                    caseData.getApplicant1().getAddress() != null && caseData.getApplicant1().getAddress().getPostTown() != null
                    ? caseData.getApplicant1().getAddress().getPostTown() : null)
                .applicant2Town(
                    caseData.getApplicant2().getAddress() != null && caseData.getApplicant2().getAddress().getPostTown() != null
                    ? caseData.getApplicant2().getAddress().getPostTown() : null)
                .caseLink(CaseLink.builder()
                    .caseReference(String.valueOf(caseDetail.getId()))
                    .build())
                .build();
        }).toList();
    }

    private CaseData getCaseData(Map<String, Object> data) {
        return objectMapper.convertValue(data, CaseData.class);
    }

    public void addMatches(CaseData data, List<CaseMatch> newMatches) {
        List<ListValue<CaseMatch>> storedMatches = data.getCaseMatches();
        log.info(" addmatches stored count: " + storedMatches.size());

        Set<String> existingCaseReferences = storedMatches.stream()
            .map(match -> match.getValue().getCaseLink().getCaseReference())
            .collect(Collectors.toSet());

        List<CaseMatch> filteredNewMatches = newMatches.stream()
            .filter(match -> !existingCaseReferences.contains(match.getCaseLink().getCaseReference()))
            .toList();

        // Convert filtered new matches to ListValue and add them to storedMatches
        storedMatches.addAll(filteredNewMatches.stream()
            .map(match -> ListValue.<CaseMatch>builder().value(match).build())
            .toList());
    }
}
