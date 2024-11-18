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
    public static final String WILDCARD_SEARCH = ".*";
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
        log.info("Case ID: " + details.getId() + " case matching search result: " + caseMatchDetails.size());

        List<CaseMatch> newMatches = transformToMatchingCasesList(caseMatchDetails);
        setToNewMatches(caseData, newMatches);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }

    public String[] normalizeAndSplit(String name) {
        // remove brackets and anything inside them
        String nameWithoutStuffInBrackets = name.replaceAll("\\([^)]*\\)", "");

        // replace multiple consecutive "/" with a single "/" because we might have multiple names due to translations
        String nameWithoutExtraSlashes = nameWithoutStuffInBrackets.replaceAll("/+", "/");

        // remove illegal characters that we've spotted in prod data
        String illegalCharacters = ".=!*_";
        String nameWithoutSpacesAroundSlashes = nameWithoutExtraSlashes.replaceAll("\\s*/\\s*", "/");
        String cleanedName = nameWithoutSpacesAroundSlashes.replaceAll("[" + illegalCharacters + "]", "").trim();
        // check for / and split to more names if it's there : prod data has this

        return cleanedName.contains("/")
            ? cleanedName.split("/", 4)  // Split into at most 4 parts to avoid getting into bad calc
            : new String[]{cleanedName};

    }

    String generateRegexPattern(String name) {
        // Split the cleaned name into parts by whitespace
        String[] nameParts = name.split("\\s+");
        // Join the parts into a regex pattern with .* between tokens
        return WILDCARD_SEARCH + String.join(WILDCARD_SEARCH, nameParts) + WILDCARD_SEARCH;
    }

    public List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> getFreshMatches(CaseDetails<CaseData, State> details,
                                                                                  MarriageDetails marriageDetails) {
        // clean the names
        String[] applicant1Names = normalizeAndSplit(marriageDetails.getApplicant1Name());
        String[] applicant2Names = normalizeAndSplit(marriageDetails.getApplicant2Name());

        BoolQueryBuilder nameMatching = QueryBuilders.boolQuery();

        // handle all combinations of name1 and name2
        for (String name1 : applicant1Names) {
            for (String name2 : applicant2Names) {
                // applicant1 might be applicant2 on another case and vice versa
                BoolQueryBuilder sameOrderCombo = QueryBuilders.boolQuery()
                    .filter(createRegexQuery("data.marriageApplicant1Name.keyword", name1))
                    .filter(createRegexQuery("data.marriageApplicant2Name.keyword", name2));

                BoolQueryBuilder oppOrderCombo = QueryBuilders.boolQuery()
                    .filter(createRegexQuery("data.marriageApplicant1Name.keyword", name2))
                    .filter(createRegexQuery("data.marriageApplicant2Name.keyword", name1));

                nameMatching.should(sameOrderCombo).should(oppOrderCombo);
            }
        }
        LocalDate marriageDate = marriageDetails.getDate();
        List<String> stateValues = POST_SUBMISSION_STATES.stream().map(State::name).toList();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termsQuery(STATE_KEY, stateValues))
            .mustNot(QueryBuilders.termQuery(REFERENCE_KEY, String.valueOf(details.getId())))
            .filter(QueryBuilders.termQuery("data.marriageDate", marriageDate.format(ES_DATE_FORMATTER)))
            .filter(nameMatching);

        final var user = idamService.retrieveSystemUpdateUserDetails();
        final var serviceAuth = authTokenGenerator.generate();
        return ccdSearchService.searchForAllCasesWithQuery(boolQuery, user, serviceAuth);
    }

    // Helper method to create name match query
    private BoolQueryBuilder createRegexQuery(String field, String cleanedName) {
        String[] nameParts = cleanedName.split("\\s+");
        String regexpPattern = ".*" + String.join(".*", nameParts) + ".*";
        return QueryBuilders.boolQuery()
            .should(QueryBuilders.regexpQuery(field, regexpPattern));
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
}
