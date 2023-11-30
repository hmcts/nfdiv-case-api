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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAosOverdue.SYSTEM_PROGRESS_TO_AOS_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemProgressCasesToAosOverdueTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemProgressCasesToAosOverdueTask progressCasesToAosOverdueTask;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(
                boolQuery()
                    .should(matchQuery(STATE, AwaitingAos))
                    .should(matchQuery(STATE, AosDrafted))
                    .minimumShouldMatch(1)
            )
            .filter(rangeQuery(DUE_DATE).lt(LocalDate.now()));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerAosOverdueTaskOnEachCaseWhenCaseDueDateIsBeforeCurrentDateOnly() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos, AosDrafted))
            .thenReturn(caseDetailsList);

        when(caseDetails2.getId()).thenReturn(2L);

        progressCasesToAosOverdueTask.run();

        verify(ccdUpdateService).submitEvent(2L, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldIgnoreCaseWhenDueDateIsNull() {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dueDate", null);

        when(caseDetails.getData()).thenReturn(caseDataMap);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos, AosDrafted))
            .thenReturn(List.of(caseDetails));

        when(caseDetails.getId()).thenReturn(TEST_CASE_ID);

        progressCasesToAosOverdueTask.run();

        verify(ccdUpdateService, never()).submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotTriggerAosOverdueTaskOnEachCaseWhenCaseDueDateIsAfterCurrentDate() {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        when(caseDetails.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(5).toString()));

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos, AosDrafted))
            .thenReturn(singletonList(caseDetails));

        progressCasesToAosOverdueTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos, AosDrafted))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        progressCasesToAosOverdueTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusDays(5).toString()));

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos, AosDrafted))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);

        progressCasesToAosOverdueTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never()).submitEvent(2L, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos, AosDrafted))
            .thenReturn(caseDetailsList);

        when(caseDetails2.getId()).thenReturn(2L);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(2L, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);

        progressCasesToAosOverdueTask.run();

        verify(ccdUpdateService).submitEvent(2L, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }
}
