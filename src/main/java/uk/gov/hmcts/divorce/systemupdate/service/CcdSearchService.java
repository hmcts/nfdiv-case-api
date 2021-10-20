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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.elasticsearch.search.sort.SortOrder.DESC;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;

@Service
@Slf4j
public class CcdSearchService {

    @Value("${core_case_data.search.page_size}")
    private int pageSize;

    @Value("${bulk-action.page-size}")
    @Setter
    private int bulkActionPageSize;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    private static final String DUE_DATE = "data.dueDate";
    private static final String STATE = "state";

    public SearchResult searchForCasesInHolding(final int from,
                                                final int size,
                                                final User user,
                                                final String serviceAuth) {

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort("data.issueDate", ASC)
            .query(boolQuery().must(matchQuery(STATE, Holding)))
            .from(from)
            .size(size);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            CASE_TYPE,
            sourceBuilder.toString());
    }

    public List<CaseDetails> searchForAllCasesWithStateOf(final State state, User user, String serviceAuth) {
        return searchForAllCasesWithStateOf(state, null, user, serviceAuth);
    }

    public List<CaseDetails> searchForAllCasesWithStateOf(final State state, final String notificationFlag,
                                                          User user, String serviceAuth) {

        final List<CaseDetails> allCaseDetails = new ArrayList<>();
        int from = 0;
        int totalResults = pageSize;

        try {
            while (totalResults == pageSize) {
                final SearchResult searchResult =
                    Holding.equals(state)
                        ? searchForCasesInHolding(from, pageSize, user, serviceAuth)
                        : searchForCasesWithStateOfDueDateBeforeWithoutFlagSet(state, from, pageSize, notificationFlag, user, serviceAuth);

                allCaseDetails.addAll(searchResult.getCases());

                from += pageSize;
                totalResults = searchResult.getTotal();
            }
        } catch (final FeignException e) {

            final String message = String.format("Failed to complete search for Cases with state of %s", state);
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }

        return allCaseDetails;
    }

    public SearchResult searchForCasesWithStateOfDueDateBeforeWithoutFlagSet(final State state,
                                                                             final int from,
                                                                             final int size,
                                                                             final String notificationFlag,
                                                                             final User user,
                                                                             final String serviceAuth) {

        BoolQueryBuilder query = Objects.isNull(notificationFlag)
            ? boolQuery()
            .must(matchQuery(STATE, state))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            : boolQuery()
            .must(matchQuery(STATE, state))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .mustNot(matchQuery(String.format("data.%s", notificationFlag), YesOrNo.YES));

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, DESC)
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
            .sort("data.issueDate", ASC)
            .query(
                boolQuery()
                    .should(boolQuery().mustNot(existsQuery("data.dataVersion")))
                    .should(boolQuery().must(rangeQuery("data.dataVersion").lt(latestVersion)))
            )
            .from(0)
            .size(2000);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            CASE_TYPE,
            sourceBuilder.toString()
        ).getCases();
    }

    public List<CaseDetails> searchAwaitingPronouncementCases(User user, String serviceAuth) {
        try {
            int from = 0;
            QueryBuilder stateQuery = matchQuery(STATE, AwaitingPronouncement);
            QueryBuilder bulkListingCaseId = existsQuery("data.bulkListCaseReference");

            QueryBuilder query = boolQuery()
                .must(stateQuery)
                .mustNot(bulkListingCaseId);

            SearchSourceBuilder sourceBuilder = SearchSourceBuilder
                .searchSource()
                .query(query)
                .from(from)
                .size(bulkActionPageSize);

            SearchResult result = coreCaseDataApi.searchCases(
                user.getAuthToken(),
                serviceAuth,
                CASE_TYPE,
                sourceBuilder.toString());

            log.info("Total cases retrieved for bulk case creation {} ", result.getTotal());
            if (!result.getCases().isEmpty()) {
                return result.getCases();
            }
        } catch (final FeignException e) {
            final String message = "Failed to complete search for Cases with state of AwaitingPronouncement";
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }

        return Collections.emptyList();
    }
}
