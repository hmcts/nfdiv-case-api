package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAwaitingFinalOrder.SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemProgressCasesToAwaitingFinalOrderTask.DATA_DATE_FINAL_ORDER_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemProgressCasesToAwaitingFinalOrderTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemProgressCasesToAwaitingFinalOrderTask task;

    private User user;

    private static final BoolQueryBuilder query = boolQuery()
        .must(matchQuery(STATE, ConditionalOrderPronounced))
        .filter(rangeQuery(DATA_DATE_FINAL_ORDER_ELIGIBLE_FROM).lte(LocalDate.now()));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerAwaitingFinalOrderTaskOnEachCaseWhenDateFinalOrderEligibleFromIsBeforeOrSameAsCurrentDate() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("dateFinalOrderEligibleFrom", LocalDate.now().toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dateFinalOrderEligibleFrom", LocalDate.now().minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldIgnoreCaseWhenDateFinalOrderEligibleFromIsNull() {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dateFinalOrderEligibleFrom", null);

        when(caseDetails.getData()).thenReturn(caseDataMap);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(List.of(caseDetails));

        task.run();

        verify(ccdUpdateService, never())
            .submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotTriggerAwaitingFinalOrderTaskOnEachCaseWhenDateFinalOrderEligibleFromIsAfterCurrentDate() {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        when(caseDetails.getData()).thenReturn(Map.of("dateFinalOrderEligibleFrom", LocalDate.now().plusDays(5).toString()));

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(singletonList(caseDetails));

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getData()).thenReturn(Map.of("dateFinalOrderEligibleFrom", LocalDate.now().toString()));

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("dateFinalOrderEligibleFrom", LocalDate.now().toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dateFinalOrderEligibleFrom", LocalDate.now().minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }
}
