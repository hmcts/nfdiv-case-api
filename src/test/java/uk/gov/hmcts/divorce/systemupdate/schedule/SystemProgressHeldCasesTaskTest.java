package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.time.LocalDate.parse;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;

@ExtendWith(MockitoExtension.class)
class SystemProgressHeldCasesTaskTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @InjectMocks
    private SystemProgressHeldCasesTask awaitingConditionalOrderTask;

    @Test
    void shouldTriggerAwaitingConditionalOrderOnEachCaseWhenCaseHasFinishedHoldingPeriod() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);
        final LocalDate issueDate1 = parse("2021-01-01");f
        final LocalDate issueDate2 = parse("2021-01-02");
        final LocalDate issueDate3 = parse("2020-12-02");

        when(caseDetails1.getData()).thenReturn(Map.of("issueDate", issueDate1.toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("issueDate", issueDate2.toString()));
        when(caseDetails3.getData()).thenReturn(Map.of("issueDate", issueDate3.toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        when(holdingPeriodService.isHoldingPeriodFinished(issueDate1)).thenReturn(true);
        when(holdingPeriodService.isHoldingPeriodFinished(issueDate2)).thenReturn(true);
        when(holdingPeriodService.isHoldingPeriodFinished(issueDate3)).thenReturn(false);
        when(holdingPeriodService.getHoldingPeriodInWeeks()).thenReturn(14);
        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_PROGRESS_HELD_CASE);

        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldIgnoreCaseWhenIssueDateIsNull() {
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
        final LocalDate issueDate = parse("2021-01-01");

        when(caseDetails1.getData()).thenReturn(Map.of("issueDate", issueDate.toString()));

        when(holdingPeriodService.isHoldingPeriodFinished(issueDate)).thenReturn(true);
        when(holdingPeriodService.getHoldingPeriodInWeeks()).thenReturn(14);
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
        final LocalDate issueDate1 = parse("2021-01-01");
        final LocalDate issueDate2 = parse("2021-01-02");

        when(caseDetails1.getData()).thenReturn(Map.of("issueDate", issueDate1.toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("issueDate", issueDate2.toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(holdingPeriodService.isHoldingPeriodFinished(issueDate1)).thenReturn(true);
        when(holdingPeriodService.isHoldingPeriodFinished(issueDate2)).thenReturn(true);
        when(holdingPeriodService.getHoldingPeriodInWeeks()).thenReturn(14);
        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_PROGRESS_HELD_CASE);
    }
}
