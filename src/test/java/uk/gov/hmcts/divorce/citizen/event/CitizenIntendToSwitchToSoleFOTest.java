package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1IntendToSwitchToSoleFoNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2IntendToSwitchToSoleFoNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.time.LocalDate;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenIntendToSwitchToSoleFO.INTEND_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CitizenIntendToSwitchToSoleFOTest {

    private static final String DUMMY_AUTH_TOKEN = "ASAFSDFASDFASDFASDFASDF";

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private Applicant1IntendToSwitchToSoleFoNotification applicant1IntendToSwitchToSoleFoNotification;

    @Mock
    private Applicant2IntendToSwitchToSoleFoNotification applicant2IntendToSwitchToSoleFoNotification;

    @Mock
    private Clock clock;

    @InjectMocks
    private CitizenIntendToSwitchToSoleFO citizenIntendToSwitchToSoleFO;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenIntendToSwitchToSoleFO.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(INTEND_SWITCH_TO_SOLE_FO);
    }

    @Test
    void shouldSetApplicant1IntendsFieldsIfTriggeredByApplicant1() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);

        AboutToStartOrSubmitResponse<CaseData, State> response = citizenIntendToSwitchToSoleFO.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getDoesApplicant1IntendToSwitchToSole()).isEqualTo(YES);
        assertThat(response.getData().getFinalOrder().getDateApplicant1DeclaredIntentionToSwitchToSoleFo()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void shouldSetApplicant2IntendsFieldsIfTriggeredByApplicant2() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(ccdAccessService.isApplicant2(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);

        AboutToStartOrSubmitResponse<CaseData, State> response = citizenIntendToSwitchToSoleFO.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getDoesApplicant2IntendToSwitchToSole()).isEqualTo(YES);
        assertThat(response.getData().getFinalOrder().getDateApplicant2DeclaredIntentionToSwitchToSoleFo()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void shouldSendApplicant1IntendsNotificationIfEventTriggeredByApplicant1() {

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);

        citizenIntendToSwitchToSoleFO.submitted(details, details);

        verify(notificationDispatcher).send(applicant1IntendToSwitchToSoleFoNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendApplicant2IntendsNotificationIfEventTriggeredByApplicant2() {

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(ccdAccessService.isApplicant2(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);

        citizenIntendToSwitchToSoleFO.submitted(details, details);

        verify(notificationDispatcher).send(applicant2IntendToSwitchToSoleFoNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldNotSendNotificationsIfEventNotTriggeredByApplicant1OrApplicant2() {

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(ccdAccessService.isApplicant2(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(false);

        citizenIntendToSwitchToSoleFO.submitted(details, details);

        verifyNoInteractions(notificationDispatcher);
    }
}
