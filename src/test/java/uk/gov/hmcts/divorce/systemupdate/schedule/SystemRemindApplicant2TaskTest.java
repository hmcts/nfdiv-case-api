package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant2Notification;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
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
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant2.SYSTEM_REMIND_APPLICANT2;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

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

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private User user;

    private static final String FLAG = "applicant2ReminderSent";

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

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

        CaseInvite caseInvite = CaseInvite.builder()
            .accessCode("123456789")
            .build();

        caseData1.setCaseInvite(caseInvite);
        caseData2.setCaseInvite(caseInvite);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(4)));
        when(caseDetails1.getId()).thenReturn(1L);
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(10)));
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(4)), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(10)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemRemindApplicant2Task.run();

        verify(applicationSentForReviewApplicant2Notification).sendReminder(caseData1, caseDetails1.getId());
        verify(applicationSentForReviewApplicant2Notification, times(0)).sendReminder(caseData2, caseDetails2.getId());
        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSendReminderEmailToApplicant2IfApplicant2ReminderSent() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now())
            .application(Application.builder()
                .applicant2ReminderSent(YES)
                .build())
            .build();

        final CaseInvite caseInvite = CaseInvite.builder()
            .accessCode("123456789")
            .build();

        caseData1.setCaseInvite(caseInvite);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(4)));
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(4)), CaseData.class)).thenReturn(caseData1);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemRemindApplicant2Task.run();

        verifyNoInteractions(applicationSentForReviewApplicant2Notification, ccdUpdateService);
    }

    @Test
    void shouldNotTriggerSystemRemindApplicant2TaskOnEachCaseWhenCaseNotTenDaysPastOriginalInvite() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(10))
            .build();

        when(caseDetails.getData()).thenReturn(Map.of("dueDate", LocalDateTime.now().plusDays(10)));
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails));

        systemRemindApplicant2Task.run();

        verifyNoInteractions(applicationSentForReviewApplicant2Notification);
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response, FLAG, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemRemindApplicant2Task.run();

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

        CaseInvite caseInvite = CaseInvite.builder()
            .accessCode("123456789")
            .build();

        caseData1.setCaseInvite(caseInvite);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(4)));
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData1);
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);

        systemRemindApplicant2Task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never()).submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
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

        CaseInvite caseInvite = CaseInvite.builder()
            .accessCode("123456789")
            .build();

        caseData1.setCaseInvite(caseInvite);
        caseData2.setCaseInvite(caseInvite);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(4)));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(3)));
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(4)), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(3)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);

        systemRemindApplicant2Task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
    }
}
