package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAosOverdue.SYSTEM_PROGRESS_TO_AOS_OVERDUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemProgressCasesToAosOverdueTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @InjectMocks
    private SystemProgressCasesToAosOverdueTask progressCasesToAosOverdueTask;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerAosOverdueTaskOnEachCaseWhenCaseDueDateIsBeforeOrSameAsCurrentDate() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingAos, user, SERVICE_AUTHORIZATION)).thenReturn(caseDetailsList);

        progressCasesToAosOverdueTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldIgnoreCaseWhenDueDateIsNull() {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dueDate", null);

        when(caseDetails.getData()).thenReturn(caseDataMap);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingAos, user, SERVICE_AUTHORIZATION)).thenReturn(List.of(caseDetails));

        progressCasesToAosOverdueTask.run();

        verify(ccdUpdateService, never()).submitEvent(caseDetails, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotTriggerAosOverdueTaskOnEachCaseWhenCaseDueDateIsAfterCurrentDate() {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        when(caseDetails.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(5).toString()));

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingAos, user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails));

        progressCasesToAosOverdueTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingAos, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        progressCasesToAosOverdueTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().toString()));

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingAos, user, SERVICE_AUTHORIZATION)).thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);

        progressCasesToAosOverdueTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never()).submitEvent(caseDetails2, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingAos, user, SERVICE_AUTHORIZATION)).thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);

        progressCasesToAosOverdueTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, SERVICE_AUTHORIZATION);
    }
}
