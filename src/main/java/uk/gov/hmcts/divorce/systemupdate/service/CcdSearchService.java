package uk.gov.hmcts.divorce.systemupdate.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsListConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.partition;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;

@Service
@Slf4j
public class CcdSearchService {

    public static final String ACCESS_CODE = "data.accessCode";
    public static final String DUE_DATE = "data.dueDate";
    public static final String ISSUE_DATE = "data.issueDate";
    public static final String DATA = "data.%s";
    public static final String STATE = "state";
    public static final String FINAL_ORDER_ELIGIBLE_FROM_DATE = "data.dateFinalOrderEligibleFrom";
    public static final String FINAL_ORDER_ELIGIBLE_TO_RESPONDENT_DATE = "data.dateFinalOrderEligibleToRespondent";
    public static final String FINAL_ORDER_SUBMITTED_DATE = "data.dateFinalOrderSubmitted";
    public static final String APPLICATION_TYPE = "applicationType";
    public static final String SOLE_APPLICATION = "soleApplication";
    public static final String APPLICANT2_REPRESENTED = "applicant2SolicitorRepresented";
    public static final String APPLICANT2_SOL_EMAIL = "applicant2SolicitorEmail";
    public static final String APPLICANT2_SOL_ORG_POLICY = "applicant2SolicitorOrganisationPolicy";
    public static final String SERVICE_METHOD = "serviceMethod";
    public static final String COURT_SERVICE = "courtService";
    public static final String APPLICANT1_OFFLINE = "applicant1Offline";
    public static final String APPLICANT2_OFFLINE = "applicant2Offline";
    public static final String APPLICANT1_PRIVATE_CONTACT = "applicant1ContactDetailsType";
    public static final String APPLICANT2_PRIVATE_CONTACT = "applicant2ContactDetailsType";
    public static final String AOS_RESPONSE = "howToRespondApplication";
    public static final String AWAITING_JS_ANSWER_START_DATE = "awaitingJsAnswerStartDate";
    public static final String SUPPLEMENTARY_CASE_TYPE = "supplementaryCaseType";

    @Value("${core_case_data.search.page_size}")
    private int pageSize;

    @Value("${bulk-action.page-size}")
    private int bulkActionPageSize;

    @Value("${core_case_data.search.total_max_results}")
    private int totalMaxResults;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private CaseDetailsListConverter caseDetailsListConverter;

    public List<CaseDetails> searchForAllCasesWithQuery(final BoolQueryBuilder query,
                                                        final User user,
                                                        final String serviceAuth,
                                                        final State... states) {

        final Set<CaseDetails> allCaseDetails = new HashSet<>();
        int from = 0;
        int totalResults = pageSize;

        try {
            while (totalResults == pageSize && allCaseDetails.size() <= totalMaxResults) {
                final SearchResult searchResult = searchForCasesWithQuery(from, pageSize, query, user, serviceAuth);

                final List<CaseDetails> pageResults = searchResult.getCases();

                allCaseDetails.addAll(pageResults);

                from += pageSize;
                totalResults = pageResults.size();
            }
        } catch (final FeignException e) {
            final String message = String.format("Failed to complete search for Cases with state of %s", Arrays.toString(states));
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }

        return allCaseDetails.stream().toList();
    }

    public SearchResult searchForCasesWithQuery(final int from,
                                                final int size,
                                                final BoolQueryBuilder query,
                                                final User user,
                                                final String serviceAuth) {

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(from)
            .size(size);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            getCaseType(),
            sourceBuilder.toString());
    }

    public List<CaseDetails> searchForCasesWithVersionLessThan(int latestVersion, User user, String serviceAuth) {

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(boolQuery()
                        .mustNot(matchQuery("data.dataVersion", 0))
                    )
                    .must(boolQuery()
                        .should(boolQuery().mustNot(existsQuery("data.dataVersion")))
                        .should(boolQuery().must(rangeQuery("data.dataVersion").lt(latestVersion)))
                    )
                    .mustNot(matchQuery(STATE, Withdrawn))
                    .mustNot(matchQuery(STATE, Rejected))
            )
            .from(0)
            .size(500);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            getCaseType(),
            sourceBuilder.toString()
        ).getCases();
    }

    public List<CaseDetails> searchForBulkCasesWithVersionLessThan(int latestVersion, User user, String serviceAuth) {

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(boolQuery()
                        .mustNot(matchQuery("data.bulkCaseDataVersion", 0))
                    )
                    .must(boolQuery()
                        .should(boolQuery().mustNot(existsQuery("data.bulkCaseDataVersion")))
                        .should(boolQuery().must(rangeQuery("data.bulkCaseDataVersion").lt(latestVersion)))
                    )
            )
            .from(0)
            .size(500);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            BulkActionCaseTypeConfig.getCaseType(),
            sourceBuilder.toString()
        ).getCases();
    }

    public uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> searchForBulkCaseById(
        String bulkCaseId, User user, String serviceAuth) {

        final String userId = user.getUserDetails().getUid();
        final String authorization = user.getAuthToken();

        final CaseDetails bulkCaseDetails = coreCaseDataApi.readForCaseWorker(
            authorization,
            serviceAuth,
            userId,
            JURISDICTION,
            BulkActionCaseTypeConfig.getCaseType(),
            bulkCaseId);

        return caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(bulkCaseDetails);
    }

    public Deque<List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>>> searchAwaitingPronouncementCasesAllPages(
        final User user,
        final String serviceAuth) {

        final QueryBuilder stateQuery = matchQuery(STATE, AwaitingPronouncement);
        final QueryBuilder bulkListingCaseId = existsQuery("data.bulkListCaseReferenceLink.CaseReference");

        final BoolQueryBuilder query = boolQuery()
            .must(stateQuery)
            .mustNot(bulkListingCaseId);

        final List<CaseDetails> allCaseDetails = searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingPronouncement);

        return new LinkedList<>(partition(
            caseDetailsListConverter.convertToListOfValidCaseDetails(allCaseDetails),
            bulkActionPageSize));
    }

    public List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>> searchForUnprocessedOrErroredBulkCases(
        final BulkActionState state,
        final User user,
        final String serviceAuth) {

        final QueryBuilder stateQuery = matchQuery(STATE, state);
        final QueryBuilder errorCasesExist = existsQuery("data.erroredCaseDetails");
        final QueryBuilder processedCases = existsQuery("data.processedCaseDetails");

        final QueryBuilder query = boolQuery()
            .must(stateQuery)
            .must(boolQuery()
                    .should(boolQuery()
                        .must(boolQuery().mustNot(errorCasesExist))
                        .must(boolQuery().mustNot(processedCases)))
                    .should(boolQuery()
                        .must(boolQuery().must(errorCasesExist))));

        return searchForBulkCases(user, serviceAuth, query);
    }

    public List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>>
        searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(final User user, final String serviceAuth) {

        final QueryBuilder createdStateQuery = matchQuery(STATE, Created);
        final QueryBuilder listedStateQuery = matchQuery(STATE, Listed);
        final QueryBuilder bulkCaseDetailsExist = existsQuery("data.erroredCaseDetails");
        final QueryBuilder casesToBeRemovedExist = existsQuery("data.casesToBeRemoved");

        final QueryBuilder query = boolQuery()
            .must(boolQuery().must(bulkCaseDetailsExist))
            .must(boolQuery().must(casesToBeRemovedExist))
            .should(createdStateQuery)
            .should(listedStateQuery)
            .minimumShouldMatch(1);

        return searchForBulkCases(user, serviceAuth, query);
    }

    public List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>>
        searchForBulkCases(final User user, final String serviceAuth, final QueryBuilder query) {

        try {
            return searchBulkActionCases(user, serviceAuth, query).stream()
                .map(caseDetailsConverter::convertToBulkActionCaseDetailsFromReformModel)
                .toList();
        } catch (final FeignException e) {
            final String message = "Failed to complete search for Bulk Cases";
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }
    }

    public List<CaseDetails> searchForCases(
        final List<String> caseReferences,
        final User user,
        final String serviceAuth) {

        final QueryBuilder bulkCaseDetailsExist = termsQuery("reference", caseReferences);
        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(bulkCaseDetailsExist)
            )
            .from(0)
            .size(50);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            getCaseType(),
            sourceBuilder.toString()
        ).getCases();
    }

    public List<CaseDetails> searchJointApplicationsWithAccessCodePostIssueApplication(User user, String serviceAuth) {

        final QueryBuilder issueDateExist = existsQuery("data.issueDate");
        final QueryBuilder jointApplication = matchQuery("data.applicationType", "jointApplication");
        final QueryBuilder accessCodeNotEmpty = wildcardQuery("data.accessCode", "?*");

        final QueryBuilder query = boolQuery()
            .must(boolQuery().must(accessCodeNotEmpty))
            .must(boolQuery().must(issueDateExist))
            .must(boolQuery().must(jointApplication))
            .mustNot(matchQuery(STATE, Withdrawn))
            .mustNot(matchQuery(STATE, Rejected));

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(0)
            .size(500);

        log.info("Query to search joint app with access code and issue date present {} ", sourceBuilder);

        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            getCaseType(),
            sourceBuilder.toString()
        ).getCases();

        log.info("Cases retrieved joint app with access code and issue date present {}", caseDetails.size());

        return caseDetails;
    }

    public List<CaseDetails> searchJointPaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(User user, String serviceAuth) {

        final QueryBuilder applicant2OfflineExist = existsQuery("data.applicant2Offline");
        final QueryBuilder jointApplication = matchQuery("data.applicationType", "jointApplication");
        final QueryBuilder newPaperCase = matchQuery("data.newPaperCase", YesOrNo.YES);

        final QueryBuilder query = boolQuery()
            .must(boolQuery().must(newPaperCase))
            .must(boolQuery().must(jointApplication))
            .must(boolQuery().mustNot(applicant2OfflineExist))
            .mustNot(matchQuery(STATE, Withdrawn))
            .mustNot(matchQuery(STATE, Rejected));

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(0)
            .size(500);

        log.info("Query to search joint paper applications where applicant 2 offline flag should be set {} ", sourceBuilder);

        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            getCaseType(),
            sourceBuilder.toString()
        ).getCases();

        log.info("Cases retrieved joint paper applications where applicant 2 offline flag should be set {}", caseDetails.size());

        return caseDetails;
    }

    public List<CaseDetails> searchSolePaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(User user, String serviceAuth) {

        final QueryBuilder applicant2OfflineExist = existsQuery("data.applicant2Offline");
        final QueryBuilder soleApplication = matchQuery("data.applicationType", "soleApplication");
        final QueryBuilder newPaperCase = matchQuery("data.newPaperCase", YesOrNo.YES);
        final QueryBuilder applicant2EmailExist = existsQuery("data.applicant2Email");

        final QueryBuilder query = boolQuery()
            .must(boolQuery().must(newPaperCase))
            .must(boolQuery().must(soleApplication))
            .must(boolQuery().mustNot(applicant2OfflineExist))
            .must(boolQuery().mustNot(applicant2EmailExist))
            .mustNot(matchQuery(STATE, Withdrawn))
            .mustNot(matchQuery(STATE, Rejected));

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(0)
            .size(500);

        log.info("Query to search sole paper applications where applicant 2 offline flag should be set {} ", sourceBuilder);

        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            getCaseType(),
            sourceBuilder.toString()
        ).getCases();

        log.info("Cases retrieved sole paper applications where applicant 2 offline flag should be set {}", caseDetails.size());

        return caseDetails;
    }

    public List<CaseDetails> searchCasesInAwaitingAosWhereConfirmReadPetitionIsYes(User user, String serviceAuth) {

        final QueryBuilder confirmReadPetitionYes = matchQuery("data.confirmReadPetition", YesOrNo.YES);
        final QueryBuilder awaitingAosState = matchQuery(STATE, AwaitingAos);

        final QueryBuilder query = boolQuery()
            .must(boolQuery().must(confirmReadPetitionYes))
            .must(boolQuery().must(awaitingAosState));

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(0)
            .size(500);

        log.info("Query to search AwaitingAOS cases with confirmReadPetition equals Yes {} ", sourceBuilder);

        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            getCaseType(),
            sourceBuilder.toString()
        ).getCases();

        log.info("Cases retrieved AwaitingAOS cases with confirmReadPetition equals Yes {}", caseDetails.size());

        return caseDetails;
    }

    private Set<CaseDetails> searchBulkActionCases(final User user, final String serviceAuth, final QueryBuilder query) {

        final Set<CaseDetails> allCaseDetails = new HashSet<>();
        int from = 0;
        int totalResults = pageSize;

        while (totalResults == pageSize) {

            final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
                .searchSource()
                .query(query)
                .from(from)
                .size(pageSize);

            final SearchResult searchResult = coreCaseDataApi.searchCases(
                user.getAuthToken(),
                serviceAuth,
                BulkActionCaseTypeConfig.getCaseType(),
                sourceBuilder.toString());

            final List<CaseDetails> pageResults = searchResult.getCases();
            allCaseDetails.addAll(pageResults);

            from += pageSize;
            totalResults = pageResults.size();
        }

        return allCaseDetails;
    }
}
