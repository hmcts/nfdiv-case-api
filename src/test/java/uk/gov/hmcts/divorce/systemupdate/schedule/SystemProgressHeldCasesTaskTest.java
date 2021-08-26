package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.solicitor.notification.AwaitingConditionalOrderNotification;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.divorce.testutil.TestConstants;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.LocalDate.parse;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private AwaitingConditionalOrderNotification conditionalOrderNotification;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SystemProgressHeldCasesTask awaitingConditionalOrderTask;

    @Test
    void shouldTriggerAwaitingConditionalOrderOnEachCaseAndSendNotificationWhenCaseHasFinishedHoldingPeriod() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);
        final LocalDate issueDate1 = parse("2021-01-01");
        final LocalDate issueDate2 = parse("2021-01-02");
        final LocalDate issueDate3 = parse("2020-12-02");

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        mockInteractions(caseDetails1, issueDate1, caseDataMap(caseDetails1, issueDate1));
        mockInteractions(caseDetails2, issueDate2, caseDataMap(caseDetails1, issueDate2));
        mockInteractions(caseDetails3, issueDate3, caseDataMap(caseDetails1, issueDate3));

        when(holdingPeriodService.isHoldingPeriodFinished(issueDate1)).thenReturn(true);
        when(holdingPeriodService.isHoldingPeriodFinished(issueDate2)).thenReturn(true);
        when(holdingPeriodService.isHoldingPeriodFinished(issueDate3)).thenReturn(false);
        when(holdingPeriodService.getHoldingPeriodInWeeks()).thenReturn(14);
        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        doNothing().when(conditionalOrderNotification).send(any(CaseData.class), anyLong());

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_PROGRESS_HELD_CASE);

        verify(conditionalOrderNotification, times(2)).send(any(CaseData.class), anyLong());
        verifyNoMoreInteractions(ccdUpdateService, conditionalOrderNotification);
    }

    @Test
    void shouldIgnoreCaseWhenIssueDateIsNull() {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails);

        mockInteractions(caseDetails, null, caseDataMap(caseDetails, null));

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService, never()).submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE);
    }

    @Test
    void shouldNotTriggerAwaitingConditionalOrderWhenCaseIsInHoldingForLessThan20Weeks() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);

        final LocalDate issueDate = LocalDate.now();
        mockInteractions(caseDetails1, issueDate, caseDataMap(caseDetails1, issueDate));

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

        mockInteractions(caseDetails1, issueDate, caseDataMap(caseDetails1, issueDate));

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

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(holdingPeriodService.isHoldingPeriodFinished(issueDate1)).thenReturn(true);
        when(holdingPeriodService.isHoldingPeriodFinished(issueDate2)).thenReturn(true);
        when(holdingPeriodService.getHoldingPeriodInWeeks()).thenReturn(14);
        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);

        doNothing().when(conditionalOrderNotification).send(any(CaseData.class), anyLong());

        mockInteractions(caseDetails1, issueDate1, caseDataMap(caseDetails1, issueDate1));
        mockInteractions(caseDetails2, issueDate2, caseDataMap(caseDetails2, issueDate2));

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_PROGRESS_HELD_CASE);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_PROGRESS_HELD_CASE);
        verify(conditionalOrderNotification, times(1)).send(any(CaseData.class), anyLong());
    }

    private Map<String, Object> caseDataMap(CaseDetails caseDetails, LocalDate issueDate) {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("issueDate", issueDate);
        caseDataMap.put("applicant1SolicitorRepresented", true);

        return caseDataMap;
    }

    private CaseData caseData(LocalDate issueDate) {
        return CaseData
            .builder()
            .applicant1(
                Applicant
                    .builder()
                    .solicitor(
                        Solicitor
                            .builder()
                            .email(TestConstants.TEST_SOLICITOR_EMAIL)
                            .build()
                    )
                    .build()
            )
            .application(Application.builder().issueDate(issueDate).build())
            .build();
    }

    private void mockInteractions(CaseDetails caseDetails, LocalDate issueDate, Map<String, Object> caseDataMap) {
        when(caseDetails.getData()).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData(issueDate));
    }
}
