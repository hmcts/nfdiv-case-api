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
import uk.gov.hmcts.divorce.divorcecase.model.CaseDataOldDivorce;
import uk.gov.hmcts.divorce.divorcecase.model.CaseMatch;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
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
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
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

        log.info("Case ID: " + details.getId() + " nfdiv case matching search result: " + caseMatchDetails.size());
        List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> oldcaseMatchDetails = getOldDivorceFreshMatches(marriageDetails);
        log.info("Case ID: " + details.getId() + " old divorce case matching search result: " + oldcaseMatchDetails.size());

        List<CaseMatch> newMatches = new ArrayList<>();
        newMatches.addAll(transformToMatchingCasesList(caseMatchDetails));
        newMatches.addAll(transformOldCaseToMatchingCasesList(oldcaseMatchDetails));

        setToNewMatches(caseData, newMatches);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }

    // Helper method to create term queries with variations for spaces
    private BoolQueryBuilder createNameMatchQuery(String field, String name) {
        return QueryBuilders.boolQuery()
            .should(QueryBuilders.termQuery(field, name)) // Exact match
            .should(QueryBuilders.termQuery(field, " " + name)) // Prepend space
            .should(QueryBuilders.termQuery(field, name + " ")) // Append space
            .should(QueryBuilders.termQuery(field, " " + name + " ")); // Prepend and append space
    }

    List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> getFreshMatches(CaseDetails<CaseData, State> details,
                                                                           MarriageDetails marriageDetails) {
        //NFDIV-4512 adding extra searches to cope with prepended and trailing space on the names
        String applicant1Name = marriageDetails.getApplicant1Name().trim();
        String applicant2Name = marriageDetails.getApplicant2Name().trim();

        BoolQueryBuilder nameMatchQuery1 = QueryBuilders.boolQuery()
            .filter(createNameMatchQuery("data.marriageApplicant1Name.keyword", applicant1Name))
            .filter(createNameMatchQuery("data.marriageApplicant2Name.keyword", applicant2Name));

        BoolQueryBuilder nameMatchQuery2 = QueryBuilders.boolQuery()
            .filter(createNameMatchQuery("data.marriageApplicant1Name.keyword", applicant2Name))
            .filter(createNameMatchQuery("data.marriageApplicant2Name.keyword", applicant1Name));

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

    public List<CaseMatch> transformOldCaseToMatchingCasesList(
        List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseMatchDetails) {

        if (caseMatchDetails == null || caseMatchDetails.isEmpty()) {
            return Collections.emptyList();
        }

        return caseMatchDetails.stream()
            .map(caseDetail -> {
                CaseDataOldDivorce caseData = getCaseDataOldDivorce(caseDetail.getData());

                // Handle potential null values in fields by using Optional
                return CaseMatch.builder()
                    .applicant1Name(Optional.ofNullable(caseData.getD8MarriagePetitionerName()).orElse(""))
                    .applicant2Name(Optional.ofNullable(caseData.getD8MarriageRespondentName()).orElse(""))
                    .date(Optional.ofNullable(caseData.getD8MarriageDate())
                        .map(LocalDate::parse)
                        .orElse(null))
                    .applicant1Postcode(Optional.ofNullable(caseData.getD8PetitionerPostCode()).orElse(""))
                    .applicant2Postcode(Optional.ofNullable(caseData.getD8RespondentPostCode()).orElse(""))
                    .applicant1Town(Optional.ofNullable(caseData.getD8PetitionerPostTown()).orElse(""))
                    .applicant2Town(Optional.ofNullable(caseData.getD8RespondentPostTown()).orElse(""))
                    .caseLink(CaseLink.builder()
                        .caseReference(String.valueOf(caseDetail.getId()))
                        .build())
                    .build();
            })
            .toList();
    }

    private CaseData getCaseData(Map<String, Object> data) {
        return objectMapper.convertValue(data, CaseData.class);
    }

    private CaseDataOldDivorce getCaseDataOldDivorce(Map<String, Object> data) {
        return objectMapper.convertValue(data, CaseDataOldDivorce.class);
    }

    public void setToNewMatches(CaseData data, List<CaseMatch> newMatches) {
        List<ListValue<CaseMatch>> storedMatches = data.getCaseMatches();
        storedMatches.clear();

        if (!newMatches.isEmpty()) {
            storedMatches.addAll(newMatches.stream()
                .map(match -> ListValue.<CaseMatch>builder().value(match).build())
                .toList());
        }
        if (storedMatches.isEmpty()) {
            data.setCaseMatches(null);
        }
    }

    List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> getOldDivorceFreshMatches(MarriageDetails marriageDetails) {
        BoolQueryBuilder nameMatchQuery1 = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery("data.D8MarriageRespondentName.keyword", marriageDetails.getApplicant1Name()))
            .filter(QueryBuilders.termQuery("data.D8MarriagePetitionerName.keyword", marriageDetails.getApplicant2Name()));

        BoolQueryBuilder nameMatchQuery2 = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery("data.D8MarriageRespondentName.keyword", marriageDetails.getApplicant2Name()))
            .filter(QueryBuilders.termQuery("data.D8MarriagePetitionerName.keyword", marriageDetails.getApplicant1Name()));

        BoolQueryBuilder nameMatching = QueryBuilders.boolQuery()
            .should(nameMatchQuery1)
            .should(nameMatchQuery2);

        LocalDate marriageDate = marriageDetails.getDate();

        BoolQueryBuilder oldDivorceQuery = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery("data.D8MarriageDate", marriageDate.format(ES_DATE_FORMATTER)))
            .filter(nameMatching);
        final var user = idamService.retrieveOldSystemUpdateUserDetails();
        final var serviceAuth = authTokenGenerator.generate();

        return ccdSearchService.searchForOldDivorceCasesWithQuery(oldDivorceQuery, user, serviceAuth);
    }
}
