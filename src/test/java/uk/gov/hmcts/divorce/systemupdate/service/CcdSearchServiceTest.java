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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsListConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class CcdSearchServiceTest {

    public static final int PAGE_SIZE = 100;
    public static final int BULK_LIST_MAX_PAGE_SIZE = 50;
    public static final int TOTAL_MAX_RESULTS = 150;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CaseDetailsListConverter caseDetailsListConverter;

    @InjectMocks
    private CcdSearchService ccdSearchService;

    @BeforeEach
    void setPageSize() {
        setField(ccdSearchService, "pageSize", PAGE_SIZE);
        setField(ccdSearchService, "bulkActionPageSize", BULK_LIST_MAX_PAGE_SIZE);
        setField(ccdSearchService, "totalMaxResults", TOTAL_MAX_RESULTS);
    }

    @Test
    void shouldReturnCasesWithGivenStateBeforeTodayWithoutFlag() {
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final int from = 0;
        final int pageSize = 100;
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();
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
            getCaseType(),
            sourceBuilder.toString()))
            .thenReturn(expected);

        final SearchResult result = ccdSearchService.searchForCasesWithQuery(from, pageSize, query, user, SERVICE_AUTHORIZATION);

        assertThat(result.getTotal()).isEqualTo(100);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReturnAllCasesWithGivenState() {
        final int totalCases = 101;
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(totalCases)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();
        final SearchResult expected2 = SearchResult.builder().total(totalCases)
            .cases(createCaseDetailsList(1, PAGE_SIZE + 1)).build();

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            getSourceBuilder(0, PAGE_SIZE).toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            getSourceBuilder(PAGE_SIZE, PAGE_SIZE).toString()))
            .thenReturn(expected2);

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Submitted);

        assertThat(searchResult.size()).isEqualTo(totalCases);
    }

    @Test
    void shouldNotReturnDuplicateCases() {
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        final List<CaseDetails> caseDetailsList = createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID);
        final List<CaseDetails> duplicateCasesDetailsList = createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID);

        final SearchResult searchResult1 = SearchResult.builder().total(PAGE_SIZE)
            .cases(caseDetailsList).build();
        final SearchResult searchResult2 = SearchResult.builder().total(PAGE_SIZE)
            .cases(duplicateCasesDetailsList).build();

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            getSourceBuilder(0, PAGE_SIZE).toString()))
            .thenReturn(searchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            getSourceBuilder(PAGE_SIZE, PAGE_SIZE).toString()))
            .thenReturn(searchResult2);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            getSourceBuilder(PAGE_SIZE * 2, PAGE_SIZE).toString()))
            .thenReturn(SearchResult.builder().total(PAGE_SIZE)
                .cases(emptyList()).build());

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Submitted);

        assertThat(searchResult.size()).isEqualTo(100);
        assertThat(searchResult).isEqualTo(new HashSet<>(caseDetailsList).stream().toList());
    }

    @Test
    void shouldReturnAllCasesInHolding() {
        final int totalCases = 101;
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Holding))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(totalCases).cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID))
            .build();
        final SearchResult expected2 = SearchResult.builder().total(totalCases).cases(createCaseDetailsList(1, PAGE_SIZE + 1))
            .build();

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
            getCaseType(),
            sourceBuilder1.toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            sourceBuilder2.toString()))
            .thenReturn(expected2);

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Holding);

        assertThat(searchResult.size()).isEqualTo(totalCases);
    }

    @Test
    void shouldReturnAllCasesWithGivenStateWhenFlagIsPassed() {

        final int totalCases = 101;
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final int from = 0;
        final SearchResult expected = SearchResult.builder().total(totalCases)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();
        final SearchResult expected2 = SearchResult.builder().total(totalCases)
            .cases(createCaseDetailsList(1, PAGE_SIZE + 1)).build();
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
            getCaseType(),
            sourceBuilder.toString()))
            .thenReturn(expected);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            sourceBuilder2.toString()))
            .thenReturn(expected2);

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response);

        assertThat(searchResult.size()).isEqualTo(totalCases);
    }

    @Test
    void shouldReturnCasesWithVersionOlderThan() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();

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
                    .mustNot(matchQuery(STATE, Withdrawn))
                    .mustNot(matchQuery(STATE, Rejected))
            )
            .from(0)
            .size(500);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            sourceBuilder.toString()))
            .thenReturn(expected1);

        final List<CaseDetails> searchResult = ccdSearchService.searchForCasesWithVersionLessThan(1, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(100);
    }

    @Test
    void shouldReturnBulkCasesWithVersionOlderThan() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();

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
            .size(500);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.getCaseType(),
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
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        doThrow(feignException(422, "A reason")).when(coreCaseDataApi)
            .searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                getCaseType(),
                getSourceBuilder(0, PAGE_SIZE).toString());

        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Submitted));

        assertThat(exception.getMessage()).contains("Failed to complete search for Cases with state of [Submitted]");
    }

    @Test
    void shouldReturnAllPagesOfCasesInStateAwaitingPronouncement() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult searchResult1 = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();
        final SearchResult searchResult2 = SearchResult.builder().total(1)
            .cases(createCaseDetailsList(1, PAGE_SIZE + 1)).build();
        final List<CaseDetails> expectedCases = concat(searchResult1.getCases().stream(), searchResult2.getCases().stream())
            .collect(toSet()).stream().toList();

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            searchSourceBuilderForAwaitingPronouncementCases(0).toString()))
            .thenReturn(searchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            searchSourceBuilderForAwaitingPronouncementCases(100).toString()))
            .thenReturn(searchResult2);
        when(caseDetailsListConverter.convertToListOfValidCaseDetails(expectedCases))
            .thenReturn(createConvertedCaseDetailsList(101, 1));

        final Deque<List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>>> allPages =
            ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);

        assertThat(allPages.size()).isEqualTo(3);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
        assertThat(allPages.poll().size()).isEqualTo(1);
    }

    @Test
    void shouldNotReturnDuplicateCasesInStateAwaitingPronouncement() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult searchResult1 = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();
        final SearchResult searchResult2 = SearchResult.builder().total(1)
            .cases(createCaseDetailsList(1, PAGE_SIZE + 1)).build();
        final List<CaseDetails> expectedCases = concat(searchResult1.getCases().stream(), searchResult2.getCases().stream())
            .collect(toSet()).stream().toList();
        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> caseDetailsList1 = createConvertedCaseDetailsList(50, 1);
        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> caseDetailsList2 = createConvertedCaseDetailsList(49, 51);
        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> caseDetailsList3 = createConvertedCaseDetailsList(1, 101);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            searchSourceBuilderForAwaitingPronouncementCases(0).toString()))
            .thenReturn(searchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            searchSourceBuilderForAwaitingPronouncementCases(100).toString()))
            .thenReturn(searchResult2);
        when(caseDetailsListConverter.convertToListOfValidCaseDetails(expectedCases))
            .thenReturn(Stream.of(caseDetailsList1, caseDetailsList2, caseDetailsList3)
                .flatMap(Collection::stream)
                .toList());

        final Deque<List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>>> allPages =
            ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);

        assertThat(allPages.size()).isEqualTo(2);
        assertThat(allPages.poll()).isEqualTo(caseDetailsList1);
        assertThat(allPages.poll()).isEqualTo(Stream.of(caseDetailsList2, caseDetailsList3).flatMap(Collection::stream).toList());
        assertThat(allPages.poll()).isNull();
    }

    @Test
    void shouldThrowCcdSearchFailedExceptionIfSearchingCasesInAwaitingPronouncementAllPagesFails() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        doThrow(feignException(422, "some error")).when(coreCaseDataApi)
            .searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                getCaseType(),
                searchSourceBuilderForAwaitingPronouncementCases(0).toString());

        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION));

        assertThat(exception.getMessage()).contains("Failed to complete search for Cases with state of [AwaitingPronouncement]");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnAllCasesInStatePronouncedWithCasesInErrorListOrEmptyProcessedList() {

        final int totalCases = 101;
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expectedSearchResult1 = SearchResult.builder().total(totalCases)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();
        final SearchResult expectedSearchResult2 = SearchResult.builder().total(totalCases)
            .cases(createCaseDetailsList(1, PAGE_SIZE + 1)).build();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails =
            mock(uk.gov.hmcts.ccd.sdk.api.CaseDetails.class);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.getCaseType(),
            searchSourceBuilderForPronouncedCasesWithCasesInError(0).toString()))
            .thenReturn(expectedSearchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.getCaseType(),
            searchSourceBuilderForPronouncedCasesWithCasesInError(100).toString()))
            .thenReturn(expectedSearchResult2);
        when(caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(any(CaseDetails.class)))
            .thenReturn(bulkCaseDetails);

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>> searchResult = ccdSearchService
            .searchForUnprocessedOrErroredBulkCases(Pronounced, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(totalCases);
    }

    @Test
    void shouldThrowCcdSearchCaseExceptionIfFeignExceptionIsThrown() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        doThrow(feignException(409, "some error")).when(coreCaseDataApi).searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.getCaseType(),
            searchSourceBuilderForPronouncedCasesWithCasesInError(0).toString());

        assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForUnprocessedOrErroredBulkCases(Pronounced, user, SERVICE_AUTHORIZATION),
            "Failed to complete search for Bulk Cases with state of Pronounced");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnAllCasesInStateCreatedOrListedWithCasesToBeRemoved() {

        final int totalCases = 101;
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expectedSearchResult1 = SearchResult.builder().total(totalCases)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();
        final SearchResult expectedSearchResult2 = SearchResult.builder().total(totalCases)
            .cases(createCaseDetailsList(1, PAGE_SIZE + 1)).build();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails =
            mock(uk.gov.hmcts.ccd.sdk.api.CaseDetails.class);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.getCaseType(),
            searchSourceBuilderForCreatedOrListedCasesWithCasesToBeRemoved(0).toString()))
            .thenReturn(expectedSearchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.getCaseType(),
            searchSourceBuilderForCreatedOrListedCasesWithCasesToBeRemoved(100).toString()))
            .thenReturn(expectedSearchResult2);
        when(caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(any(CaseDetails.class)))
            .thenReturn(bulkCaseDetails);

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>> searchResult = ccdSearchService
            .searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(totalCases);
    }

    @Test
    void shouldThrowCcdSearchCaseExceptionIfFeignExceptionIsThrownWhenFetchingCasesToRemove() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        doThrow(feignException(409, "some error")).when(coreCaseDataApi).searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            BulkActionCaseTypeConfig.getCaseType(),
            searchSourceBuilderForCreatedOrListedCasesWithCasesToBeRemoved(0).toString());

        assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(user, SERVICE_AUTHORIZATION),
            "Failed to complete search for Bulk Cases with state of Pronounced");
    }

    @Test
    void shouldReturnCasesFromCcdWithMatchingCaseReferences() {

        final List<String> caseReferences = List.of(
            "1643192250866023",
            "1627308042786515",
            "1627504115236368",
            "1627568021302127"
        );
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected = SearchResult.builder()
            .total(caseReferences.size())
            .cases(createCaseDetailsList(caseReferences.size(), TEST_CASE_ID))
            .build();

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(termsQuery("reference", caseReferences))
            )
            .from(0)
            .size(50);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            sourceBuilder.toString()))
            .thenReturn(expected);

        final List<CaseDetails> searchResult = ccdSearchService.searchForCases(caseReferences, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(4);
    }

    @Test
    void shouldReturnJointApplicationCasesContainingAccessCodePostIssueApplication() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();

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

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            sourceBuilder.toString()))
            .thenReturn(expected);

        final List<CaseDetails> searchResult =
            ccdSearchService.searchJointApplicationsWithAccessCodePostIssueApplication(user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(100);
    }

    @Test
    void shouldReturnCasesInAwaitingAosWhereConfirmReadPetitionIsYes() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();

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

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            sourceBuilder.toString()))
            .thenReturn(expected);

        final List<CaseDetails> searchResult =
            ccdSearchService.searchCasesInAwaitingAosWhereConfirmReadPetitionIsYes(user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(100);
    }

    @Test
    void shouldReturnJointPaperApplicationsWhereApplicant2OfflineFlagShouldBeSet() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();

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

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            sourceBuilder.toString()))
            .thenReturn(expected);

        final List<CaseDetails> searchResult =
            ccdSearchService.searchJointPaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(100);
    }

    @Test
    void shouldReturnSolePaperApplicationsWhereApplicant2OfflineFlagShouldBeSet() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, TEST_CASE_ID)).build();

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

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            sourceBuilder.toString()))
            .thenReturn(expected);

        final List<CaseDetails> searchResult =
            ccdSearchService.searchSolePaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, SERVICE_AUTHORIZATION);

        assertThat(searchResult.size()).isEqualTo(100);
    }

    @Test
    public void shouldReturnBulkCaseDetailsWithGivenCaseId() {
        final User user = new User(CASEWORKER_AUTH_TOKEN, UserInfo.builder().uid("123").build());

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getUid(),
            BulkActionCaseTypeConfig.JURISDICTION,
            BulkActionCaseTypeConfig.getCaseType(),
            "1"
        )).thenReturn(mock(CaseDetails.class));

        uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails
            = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();

        when(caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(any(CaseDetails.class)))
            .thenReturn(bulkCaseDetails);

        ccdSearchService.searchForBulkCaseById("1", user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).readForCaseWorker(CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getUid(),
            BulkActionCaseTypeConfig.JURISDICTION,
            BulkActionCaseTypeConfig.getCaseType(),
            "1");
    }

    @Test
    void shouldNotReturnMoreThanTheTotalMaxNumberOfCases() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult searchResult1 = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, 1)).build();
        final SearchResult searchResult2 = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE, PAGE_SIZE + 1)).build();
        final List<CaseDetails> expectedCases = concat(searchResult1.getCases().stream(), searchResult2.getCases().stream())
            .collect(toSet()).stream().toList();

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            searchSourceBuilderForAwaitingPronouncementCases(0).toString()))
            .thenReturn(searchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            getCaseType(),
            searchSourceBuilderForAwaitingPronouncementCases(PAGE_SIZE).toString()))
            .thenReturn(searchResult2);
        when(caseDetailsListConverter.convertToListOfValidCaseDetails(expectedCases))
            .thenReturn(createConvertedCaseDetailsList(PAGE_SIZE * 2, TEST_CASE_ID));

        final Deque<List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>>> allPages =
            ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);

        assertThat(allPages.size()).isEqualTo(4);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
    }


    private List<CaseDetails> createCaseDetailsList(final int size, final long idStart) {

        final List<CaseDetails> caseDetails = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            caseDetails.add(CaseDetails.builder().id(idStart + index).build());
        }

        return caseDetails;
    }

    @SuppressWarnings("unchecked")
    private List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> createConvertedCaseDetailsList(final int size,
                                                                                                       final long idStart) {

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> caseDetailsList = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
            caseDetails.setId(idStart + index);
            caseDetailsList.add(caseDetails);
        }

        return caseDetailsList;
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
        QueryBuilder bulkListingCaseId = existsQuery("data.bulkListCaseReferenceLink.CaseReference");

        QueryBuilder query = boolQuery()
            .must(stateQuery)
            .mustNot(bulkListingCaseId);

        return SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(from)
            .size(PAGE_SIZE);
    }

    private SearchSourceBuilder searchSourceBuilderForPronouncedCasesWithCasesInError(final int from) {
        final QueryBuilder stateQuery = matchQuery(STATE, Pronounced);
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

        return SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(from)
            .size(PAGE_SIZE);
    }

    private SearchSourceBuilder searchSourceBuilderForCreatedOrListedCasesWithCasesToBeRemoved(final int from) {
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

        return SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(from)
            .size(PAGE_SIZE);
    }
}
