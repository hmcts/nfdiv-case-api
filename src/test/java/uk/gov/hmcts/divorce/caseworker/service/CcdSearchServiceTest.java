package uk.gov.hmcts.divorce.caseworker.service;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class CcdSearchServiceTest {

    public static final int PAGE_SIZE = 100;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CcdSearchService ccdSearchService;

    @BeforeEach
    void setPageSize() {
        setField(ccdSearchService, "pageSize", 100);
    }

    @Test
    void shouldReturnCasesWithGivenStateFromZeroToPageSizeOfFifty() {

        final User caseworkerDetails = new User(CASEWORKER_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected = SearchResult.builder().build();
        final int from = 0;
        final int pageSize = 50;

        when(idamService.retrieveCaseWorkerDetails()).thenReturn(caseworkerDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(coreCaseDataApi.searchCases(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            getSourceBuilder(from, pageSize).toString()))
            .thenReturn(expected);

        final SearchResult result = ccdSearchService.searchForCaseWithStateOf(Submitted, from, pageSize);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReturnAllCasesWithGivenState() {

        final User caseworkerDetails = new User(CASEWORKER_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(1).cases(createCaseDetailsList(1)).build();

        when(idamService.retrieveCaseWorkerDetails()).thenReturn(caseworkerDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(coreCaseDataApi.searchCases(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            getSourceBuilder(0, PAGE_SIZE).toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            getSourceBuilder(PAGE_SIZE, PAGE_SIZE).toString()))
            .thenReturn(expected2);

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithStateOf(Submitted);

        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldThrowCcdSearchFailedExceptionIfSearchFails() {

        final User caseworkerDetails = new User(CASEWORKER_AUTH_TOKEN, UserDetails.builder().build());

        when(idamService.retrieveCaseWorkerDetails()).thenReturn(caseworkerDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        doThrow(feignException(422, "A reason")).when(coreCaseDataApi)
            .searchCases(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASE_TYPE,
                getSourceBuilder(0, PAGE_SIZE).toString());

        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForAllCasesWithStateOf(Submitted));

        assertThat(exception.getMessage()).contains("Failed to complete search for Cases with state of Submitted");
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
            .sort("data.issueDate", ASC)
            .query(boolQuery().must(matchQuery("state", Submitted)))
            .from(from)
            .size(pageSize);
    }
}
