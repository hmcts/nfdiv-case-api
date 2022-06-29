package uk.gov.hmcts.divorce.systemupdate.service;

import feign.FeignException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsListConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Lists.partition;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;

@Service
@Slf4j
public class CcdSearchService {

    public static final String ACCESS_CODE = "data.accessCode";
    public static final String DUE_DATE = "data.dueDate";
    public static final String ISSUE_DATE = "data.issueDate";
    public static final String DATA = "data.%s";
    public static final String STATE = "state";
    public static final String AOS_RESPONSE = "data.howToRespondApplication";
    public static final String FINAL_ORDER_ELIGIBLE_FROM_DATE = "data.dateFinalOrderEligibleFrom";
    public static final String FINAL_ORDER_ELIGIBLE_TO_RESPONDENT_DATE = "data.dateFinalOrderEligibleToRespondent";
    public static final String APPLICATION_TYPE = "applicationType";
    public static final String SOLE_APPLICATION = "soleApplication";
    public static final String APPLICANT2_REPRESENTED = "applicant2SolicitorRepresented";
    public static final String APPLICANT2_SOL_EMAIL = "applicant2SolicitorEmail";
    public static final String APPLICANT2_SOL_ORG_POLICY = "applicant2SolicitorOrganisationPolicy";
    public static final String SERVICE_METHOD = "serviceMethod";
    public static final String COURT_SERVICE = "courtService";

    @Value("${core_case_data.search.page_size}")
    private int pageSize;

    @Value("${bulk-action.page-size}")
    @Setter
    private int bulkActionPageSize;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private CaseDetailsListConverter caseDetailsListConverter;

    public List<CaseDetails> searchForAllCasesWithQuery(
        final BoolQueryBuilder query, User user, String serviceAuth, final State... states) {

        final List<CaseDetails> allCaseDetails = new ArrayList<>();
        int from = 0;
        int totalResults = pageSize;

        try {
            while (totalResults == pageSize) {
                final SearchResult searchResult =
                    searchForCasesWithQuery(from, pageSize, query, user, serviceAuth);

                allCaseDetails.addAll(searchResult.getCases());

                from += pageSize;
                totalResults = searchResult.getTotal();
            }
        } catch (final FeignException e) {
            final String message = String.format("Failed to complete search for Cases with state of %s", Arrays.toString(states));
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }

        return allCaseDetails;
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
            CASE_TYPE,
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
            )
            .from(0)
            .size(500);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            CASE_TYPE,
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
            BulkActionCaseTypeConfig.CASE_TYPE,
            sourceBuilder.toString()
        ).getCases();
    }

    public Deque<List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>>> searchAwaitingPronouncementCasesAllPages(
        final User user,
        final String serviceAuth) {

        final QueryBuilder stateQuery = matchQuery(STATE, AwaitingPronouncement);
        final QueryBuilder bulkListingCaseId = existsQuery("data.bulkListCaseReference");

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

        final List<CaseDetails> allCaseDetails = new ArrayList<>();
        int from = 0;
        int totalResults = pageSize;

        final QueryBuilder stateQuery = matchQuery(STATE, state);
        final QueryBuilder bulkCaseDetailsExist = existsQuery("data.erroredCaseDetails");
        final QueryBuilder errorCasesExist = existsQuery("data.erroredCaseDetails");
        final QueryBuilder processedCases = existsQuery("data.processedCaseDetails");

        final QueryBuilder query = boolQuery()
            .must(stateQuery)
            .must(boolQuery()
                .must(boolQuery().must(bulkCaseDetailsExist))
                .should(boolQuery().must(errorCasesExist))
                .should(boolQuery().mustNot(processedCases)));

        try {
            while (totalResults == pageSize) {

                final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
                    .searchSource()
                    .query(query)
                    .from(from)
                    .size(pageSize);

                final SearchResult searchResult = coreCaseDataApi.searchCases(
                    user.getAuthToken(),
                    serviceAuth,
                    BulkActionCaseTypeConfig.CASE_TYPE,
                    sourceBuilder.toString());

                allCaseDetails.addAll(searchResult.getCases());

                from += pageSize;
                totalResults = searchResult.getTotal();
            }
        } catch (final FeignException e) {

            final String message = String.format("Failed to complete search for Bulk Cases with state of %s", state);
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }

        return allCaseDetails.stream()
            .map(caseDetailsConverter::convertToBulkActionCaseDetailsFromReformModel)
            .collect(toList());
    }

    public List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>>
        searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(final User user, final String serviceAuth) {

        final List<CaseDetails> allCaseDetails = new ArrayList<>();
        int from = 0;
        int totalResults = pageSize;

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

        try {
            while (totalResults == pageSize) {

                final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
                    .searchSource()
                    .query(query)
                    .from(from)
                    .size(pageSize);

                final SearchResult searchResult = coreCaseDataApi.searchCases(
                    user.getAuthToken(),
                    serviceAuth,
                    BulkActionCaseTypeConfig.CASE_TYPE,
                    sourceBuilder.toString());

                allCaseDetails.addAll(searchResult.getCases());

                from += pageSize;
                totalResults = searchResult.getTotal();
            }
        } catch (final FeignException e) {
            final String message = "Failed to complete search for Bulk Cases with state of Created or Listed with cases to be removed";
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }

        return allCaseDetails.stream()
            .map(caseDetailsConverter::convertToBulkActionCaseDetailsFromReformModel)
            .collect(toList());
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
            CASE_TYPE,
            sourceBuilder.toString()
        ).getCases();
    }
}
