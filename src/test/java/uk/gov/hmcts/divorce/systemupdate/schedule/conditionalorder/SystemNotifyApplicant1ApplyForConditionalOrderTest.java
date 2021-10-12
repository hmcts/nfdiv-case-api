package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1ApplyForConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
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
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemApplicant1ApplyForConditionalOrder.SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SystemNotifyApplicant1ApplyForConditionalOrderTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private Applicant1ApplyForConditionalOrderNotification applicant1ApplyForConditionalOrderNotification;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private SystemNotifyApplicant1ApplyForConditionalOrder systemNotifyApplicant1ApplyForConditionalOrder;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private User user;

    private static final String FLAG = "applicant1NotifiedCanApplyForConditionalOrder";

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSendEmailForConditionalOrder() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().minusWeeks(21))
            .build();
        final CaseData caseData2 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(5))
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusWeeks(21)));
        when(caseDetails1.getId()).thenReturn(1L);
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(5)));
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().minusWeeks(21)), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(5)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingConditionalOrder, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemNotifyApplicant1ApplyForConditionalOrder.run();

        verify(applicant1ApplyForConditionalOrderNotification).sendToApplicant1(caseData1, caseDetails1.getId());
        verify(applicant1ApplyForConditionalOrderNotification, times(0)).sendToApplicant1(caseData2, caseDetails2.getId());
        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSendEmailForConditionalOrder() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now())
            .build();
        final CaseData caseData2 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(5))
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(5)));

        when(mapper.convertValue(Map.of("dueDate", LocalDate.now()), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(5)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingConditionalOrder, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemNotifyApplicant1ApplyForConditionalOrder.run();

        verifyNoInteractions(applicant1ApplyForConditionalOrderNotification, ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingConditionalOrder, FLAG, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemNotifyApplicant1ApplyForConditionalOrder.run();

        verifyNoInteractions(applicant1ApplyForConditionalOrderNotification, ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().minusWeeks(21))
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusWeeks(21)));
        when(caseDetails1.getId()).thenReturn(1L);

        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().minusWeeks(21)), CaseData.class)).thenReturn(caseData1);
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingConditionalOrder, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);

        systemNotifyApplicant1ApplyForConditionalOrder.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(caseDetails2, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().minusWeeks(21))
            .build();
        final CaseData caseData2 = CaseData.builder()
            .dueDate(LocalDate.now().minusWeeks(21))
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusWeeks(21)));
        when(caseDetails1.getId()).thenReturn(1L);
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusWeeks(21)));
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().minusWeeks(21)), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().minusWeeks(21)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingConditionalOrder, FLAG, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);

        systemNotifyApplicant1ApplyForConditionalOrder.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);

    }
}
