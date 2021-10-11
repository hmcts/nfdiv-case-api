package uk.gov.hmcts.divorce.systemupdate.service;

import feign.FeignException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.elasticsearch.search.sort.SortOrder.DESC;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;

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

    public SearchResult searchForCaseWithStateOf(final State state,
                                                 final int from,
                                                 final int size,
                                                 final User user,
                                                 final String serviceAuth) {

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort("data.issueDate", ASC)
            .query(boolQuery().must(matchQuery("state", state)))
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
                    notificationFlag != null
                        ? searchForCasesWithStateOfDueDateBeforeWithoutFlagSet(state, from, pageSize, notificationFlag, user, serviceAuth)
                        : searchForCaseWithStateOf(state, from, pageSize, user, serviceAuth);

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

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort("data.dueDate", DESC)
            .query(
                boolQuery()
                    .must(matchQuery("state", state))
                    .filter(rangeQuery("data.dueDate").lte(LocalDate.now()))
                    .mustNot(matchQuery(String.format("data.%s", notificationFlag), YesOrNo.YES))
            )
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

        final List<CaseDetails> allCaseDetails = new ArrayList<>();
        Set<Long> processedCaseIds = new HashSet<>();

        try {
            int from = 0;
            int totalSearch;
            do {
                QueryBuilder stateQuery = matchQuery("state", AwaitingPronouncement);
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

                from += bulkActionPageSize;
                totalSearch = result.getTotal();

                result.setCases(
                    result.getCases()
                        .stream()
                        .filter(caseDetails -> !processedCaseIds.contains(caseDetails.getId()))
                        .collect(Collectors.toList())
                );

                result.getCases()
                    .forEach(caseDetails -> processedCaseIds.add(caseDetails.getId()));

                if (!result.getCases().isEmpty()) {
                    allCaseDetails.addAll(result.getCases());
                }
            } while (from < totalSearch);
        } catch (final FeignException e) {
            final String message = "Failed to complete search for Cases with state of AwaitingPronouncement";
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }

        return allCaseDetails;
    }
}
