package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant2Notification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant2.SYSTEM_REMIND_APPLICANT2;

@ExtendWith(MockitoExtension.class)
public class SystemRemindApplicant2TaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private ApplicationSentForReviewApplicant2Notification applicationSentForReviewApplicant2Notification;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private SystemRemindApplicant2Task systemRemindApplicant2Task;

    @Test
    void shouldSendReminderEmailToApplicant2IfTenDaysSinceOriginalInviteSent() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(4))
            .build();
        final CaseData caseData2 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(10))
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(4)));
        when(caseDetails1.getId()).thenReturn(1L);
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(10)));
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(4)), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(10)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response)).thenReturn(caseDetailsList);

        systemRemindApplicant2Task.execute();

        verify(applicationSentForReviewApplicant2Notification).sendReminder(caseData1, caseDetails1.getId());
        verify(applicationSentForReviewApplicant2Notification, times(0)).sendReminder(caseData2, caseDetails2.getId());
        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2);
    }

    @Test
    void shouldNotTriggerSystemRemindApplicant2TaskOnEachCaseWhenCaseNotTenDaysPastOriginalInvite() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(10))
            .build();

        when(caseDetails.getData()).thenReturn(Map.of("dueDate", LocalDateTime.now().plusDays(10)));
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response)).thenReturn(singletonList(caseDetails));

        systemRemindApplicant2Task.execute();

        verifyNoInteractions(applicationSentForReviewApplicant2Notification);
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemRemindApplicant2Task.execute();

        verifyNoInteractions(applicationSentForReviewApplicant2Notification);
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(4))
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(4)));
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData1);
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response)).thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2);

        systemRemindApplicant2Task.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2);
        verify(ccdUpdateService, never()).submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANT2);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(4))
            .build();
        final CaseData caseData2 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(3))
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(4)));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(3)));
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(4)), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(3)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response)).thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2);

        systemRemindApplicant2Task.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANT2);
    }
}
