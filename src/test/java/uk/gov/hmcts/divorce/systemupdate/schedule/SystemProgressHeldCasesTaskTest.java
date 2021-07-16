package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.divorce.caseworker.service.CcdConflictException;
import uk.gov.hmcts.divorce.caseworker.service.CcdManagementException;
import uk.gov.hmcts.divorce.caseworker.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.caseworker.service.CcdSearchService;
import uk.gov.hmcts.divorce.caseworker.service.CcdUpdateService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;

@ExtendWith(MockitoExtension.class)
class SystemProgressHeldCasesTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @InjectMocks
    private SystemProgressHeldCasesTask awaitingConditionalOrderTask;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(awaitingConditionalOrderTask, "holdingPeriodInWeeks", 20);
    }

    @Test
    void shouldTriggerAwaitingConditionalOrderOnEachCaseWhenCaseIsInHoldingForMoreThan20Weeks() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("issueDate", "2021-01-01"));
        when(caseDetails2.getData()).thenReturn(Map.of("issueDate", "2021-01-02"));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_PROGRESS_HELD_CASE);
    }

    @Test
    void shouldIgnoreCaseWhenIssueDateIsNull()  {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = CaseData.builder().build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails);

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService, never()).submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE);
    }

    @Test
    void shouldNotTriggerAwaitingConditionalOrderWhenCaseIsInHoldingForLessThan20Weeks() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("issueDate", LocalDate.now().toString()));

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(singletonList(caseDetails1));

        awaitingConditionalOrderTask.execute();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithStateOf(Holding))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        awaitingConditionalOrderTask.execute();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getData()).thenReturn(Map.of("issueDate", "2021-01-01"));

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);
        verify(ccdUpdateService, never()).submitEvent(caseDetails2, SYSTEM_PROGRESS_HELD_CASE);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("issueDate", "2021-01-01"));
        when(caseDetails2.getData()).thenReturn(Map.of("issueDate", "2021-01-02"));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_PROGRESS_HELD_CASE);
    }
}
