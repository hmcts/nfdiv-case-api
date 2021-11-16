package uk.gov.hmcts.divorce.systemupdate.service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class CcdSearchServiceTest {

    public static final int PAGE_SIZE = 100;
    public static final int BULK_LIST_MAX_PAGE_SIZE = 50;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CcdSearchService ccdSearchService;

    @BeforeEach
    void setPageSize() {
        setField(ccdSearchService, "pageSize", 100);
        setField(ccdSearchService, "bulkActionPageSize", 50);
    }

    @Test
    void shouldReturnCasesWithGivenStateBeforeTodayWithoutFlag() {
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final int from = 0;
        final int pageSize = 100;
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, AwaitingApplicant2Response))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .mustNot(matchQuery("data.applicant2ReminderSent", YesOrNo.YES));

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(from)
            .size(pageSize);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder.toString()))
            .thenReturn(expected);

        final SearchResult result = ccdSearchService.searchForCasesWithQuery(from, pageSize, query, user, SERVICE_AUTHORIZATION);

        assertThat(result.getTotal()).isEqualTo(100);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReturnAllCasesWithGivenState() {
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(1).cases(createCaseDetailsList(1)).build();

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            getSourceBuilder(0, PAGE_SIZE).toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            getSourceBuilder(PAGE_SIZE, PAGE_SIZE).toString()))
            .thenReturn(expected2);

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(Submitted, query, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldReturnAllCasesInHolding() {
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Holding))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(1).cases(createCaseDetailsList(1)).build();

        SearchSourceBuilder sourceBuilder1 = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(
                boolQuery()
                    .must(matchQuery(STATE, Holding))
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            )
            .from(0)
            .size(PAGE_SIZE);

        SearchSourceBuilder sourceBuilder2 = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(
                boolQuery()
                    .must(matchQuery(STATE, Holding))
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            )
            .from(PAGE_SIZE)
            .size(PAGE_SIZE);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder1.toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder2.toString()))
            .thenReturn(expected2);

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldReturnAllCasesWithGivenStateWhenFlagIsPassed() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final int from = 0;
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(1).cases(createCaseDetailsList(1)).build();
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery("state", AwaitingApplicant2Response))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .mustNot(matchQuery("data.applicant2ReminderSent", YesOrNo.YES));

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(from)
            .size(PAGE_SIZE);

        SearchSourceBuilder sourceBuilder2 = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(PAGE_SIZE)
            .size(PAGE_SIZE);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder.toString()))
            .thenReturn(expected);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder2.toString()))
            .thenReturn(expected2);

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(
            AwaitingApplicant2Response, query, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldReturnCasesWithVersionOlderThan() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(boolQuery()
                        .mustNot(matchQuery("data.dataVersion", 0))
                    )
                    .must(boolQuery()
                        .should(boolQuery().mustNot(existsQuery("data.dataVersion")))
                        .should(boolQuery().must(rangeQuery("data.dataVersion").lt(1)))
                    )
            )
            .from(0)
            .size(2000);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder.toString()))
            .thenReturn(expected1);

        final List<CaseDetails> searchResult = ccdSearchService.searchForCasesWithVersionLessThan(1, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(100);
    }

    @Test
    void shouldReturnBulkCasesWithVersionOlderThan() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(boolQuery()
                        .mustNot(matchQuery("data.bulkCaseDataVersion", 0))
                    )
                    .must(boolQuery()
                        .should(boolQuery().mustNot(existsQuery("data.bulkCaseDataVersion")))
                        .should(boolQuery().must(rangeQuery("data.bulkCaseDataVersion").lt(1)))
                    )
            )
            .from(0)
            .size(2000);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.CASE_TYPE,
            sourceBuilder.toString()))
            .thenReturn(expected1);

        final List<CaseDetails> searchResult = ccdSearchService.searchForBulkCasesWithVersionLessThan(1, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(100);
    }

    @Test
    void shouldThrowCcdSearchFailedExceptionIfSearchFails() {
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery("state", Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        doThrow(feignException(422, "A reason")).when(coreCaseDataApi)
            .searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASE_TYPE,
                getSourceBuilder(0, PAGE_SIZE).toString());

        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForAllCasesWithQuery(Submitted, query, user, SERVICE_AUTHORIZATION));

        assertThat(exception.getMessage()).contains("Failed to complete search for Cases with state of Submitted");
    }

    @Test
    void shouldReturnAllPagesOfCasesInStateAwaitingPronouncement() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(BULK_LIST_MAX_PAGE_SIZE)
            .cases(createCaseDetailsList(BULK_LIST_MAX_PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(1)
            .cases(createCaseDetailsList(1)).build();

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            searchSourceBuilderForAwaitingPronouncementCases(0).toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            searchSourceBuilderForAwaitingPronouncementCases(50).toString()))
            .thenReturn(expected2);

        final Deque<List<CaseDetails>> allPages = ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);

        assertThat(allPages.size()).isEqualTo(2);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
        assertThat(allPages.poll().size()).isEqualTo(1);
    }

    @Test
    void shouldThrowCcdSearchFailedExceptionIfSearchingCasesInAwaitingPronouncementAllPagesFails() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        doThrow(feignException(422, "some error")).when(coreCaseDataApi)
            .searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASE_TYPE,
                searchSourceBuilderForAwaitingPronouncementCases(0).toString());

        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION));

        assertThat(exception.getMessage()).contains("Failed to complete search for Cases with state of AwaitingPronouncement");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnAllCasesInStatePronouncedWithCasesInErrorListOrEmptyProcessedList() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expectedSearchResult1 = SearchResult.builder().total(100)
            .cases(createCaseDetailsList(100)).build();
        final SearchResult expectedSearchResult2 = SearchResult.builder().total(1)
            .cases(createCaseDetailsList(1)).build();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails =
            mock(uk.gov.hmcts.ccd.sdk.api.CaseDetails.class);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.CASE_TYPE,
            searchSourceBuilderForPronouncedCasesWithCasesInError(0).toString()))
            .thenReturn(expectedSearchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.CASE_TYPE,
            searchSourceBuilderForPronouncedCasesWithCasesInError(100).toString()))
            .thenReturn(expectedSearchResult2);
        when(caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(any(CaseDetails.class)))
            .thenReturn(bulkCaseDetails);

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>> searchResult = ccdSearchService
            .searchForUnprocessedOrErroredBulkCases(Pronounced, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldThrowCcdSearchCaseExceptionIfFeignExceptionIsThrown() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        doThrow(feignException(409, "some error")).when(coreCaseDataApi).searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.CASE_TYPE,
            searchSourceBuilderForPronouncedCasesWithCasesInError(0).toString());

        assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForUnprocessedOrErroredBulkCases(Pronounced, user, SERVICE_AUTHORIZATION),
            "Failed to complete search for Bulk Cases with state of Pronounced");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnAllCasesInStateCreatedOrListedWithCasesToBeRemoved() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expectedSearchResult1 = SearchResult.builder().total(100)
            .cases(createCaseDetailsList(100)).build();
        final SearchResult expectedSearchResult2 = SearchResult.builder().total(1)
            .cases(createCaseDetailsList(1)).build();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails =
            mock(uk.gov.hmcts.ccd.sdk.api.CaseDetails.class);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.CASE_TYPE,
            searchSourceBuilderForCreatedOrListedCasesWithCasesToBeRemoved(0).toString()))
            .thenReturn(expectedSearchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.CASE_TYPE,
            searchSourceBuilderForCreatedOrListedCasesWithCasesToBeRemoved(100).toString()))
            .thenReturn(expectedSearchResult2);
        when(caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(any(CaseDetails.class)))
            .thenReturn(bulkCaseDetails);

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>> searchResult = ccdSearchService
            .searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldThrowCcdSearchCaseExceptionIfFeignExceptionIsThrownWhenFetchingCasesToRemove() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        doThrow(feignException(409, "some error")).when(coreCaseDataApi).searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.CASE_TYPE,
            searchSourceBuilderForCreatedOrListedCasesWithCasesToBeRemoved(0).toString());

        assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(user, SERVICE_AUTHORIZATION),
            "Failed to complete search for Bulk Cases with state of Pronounced");
    }

    private List<CaseDetails> createCaseDetailsList(final int size) {

        final List<CaseDetails> caseDetails = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            caseDetails.add(mock(CaseDetails.class));
        }

        return caseDetails;
    }

    private SearchSourceBuilder getSourceBuilder(final int from, final int pageSize) {
        return SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(boolQuery()
                .must(matchQuery(STATE, Submitted))
                .filter(rangeQuery(DUE_DATE).lte(LocalDate.now())))
            .from(from)
            .size(pageSize);
    }

    private SearchSourceBuilder searchSourceBuilderForAwaitingPronouncementCases(final int from) {
        QueryBuilder stateQuery = matchQuery(STATE, AwaitingPronouncement);
        QueryBuilder bulkListingCaseId = existsQuery("data.bulkListCaseReference");

        QueryBuilder query = boolQuery()
            .must(stateQuery)
            .mustNot(bulkListingCaseId);

        return SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(from)
            .size(BULK_LIST_MAX_PAGE_SIZE);

    }

    private SearchSourceBuilder searchSourceBuilderForPronouncedCasesWithCasesInError(final int from) {
        final QueryBuilder stateQuery = matchQuery(STATE, Pronounced);
        final QueryBuilder errorCasesExist = existsQuery("data.erroredCaseDetails");
        final QueryBuilder processedCases = existsQuery("data.processedCaseDetails");

        final QueryBuilder query = boolQuery()
            .must(stateQuery)
            .must(boolQuery()
                .should(boolQuery().must(errorCasesExist))
                .should(boolQuery().mustNot(processedCases)));

        return SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(from)
            .size(PAGE_SIZE);
    }

    private SearchSourceBuilder searchSourceBuilderForCreatedOrListedCasesWithCasesToBeRemoved(final int from) {
        final QueryBuilder createdStateQuery = matchQuery(STATE, Created);
        final QueryBuilder listedStateQuery = matchQuery(STATE, Listed);
        final QueryBuilder casesToBeRemovedExist = existsQuery("data.casesToBeRemoved");

        final QueryBuilder query = boolQuery()
            .must(boolQuery().must(casesToBeRemovedExist))
            .should(createdStateQuery)
            .should(listedStateQuery)
            .minimumShouldMatch(1);

        return SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(from)
            .size(PAGE_SIZE);
    }
}
