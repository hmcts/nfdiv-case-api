package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.Applicant1CanSwitchToSoleFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2CanSwitchToSoleFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(SpringExtension.class)
class SystemNotifyJointApplicantCanSwitchToSoleFinalOrderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Applicant1CanSwitchToSoleFinalOrderNotification applicant1CanSwitchToSoleFinalOrderNotification;

    @Mock
    private Applicant2CanSwitchToSoleFinalOrderNotification applicant2CanSwitchToSoleFinalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemNotifyJointApplicantCanSwitchToSoleFinalOrder systemNotifyJointApplicantCanSwitchToSoleFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemNotifyJointApplicantCanSwitchToSoleFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SystemNotifyJointApplicantCanSwitchToSoleFinalOrder.SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER);
    }

    @Test
    void shouldSenNotificationToApplicant1WhenApplicant2FinalOrderIsOverdue() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        caseData.setFinalOrder(FinalOrder.builder()
                    .dateFinalOrderSubmitted(LocalDate.now().minusDays(15).atStartOfDay())
                    .applicant1SubmittedFinalOrder(YES)
                    .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemNotifyJointApplicantCanSwitchToSoleFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getApplication().getJointApplicantNotifiedCanSwitchToSoleFinalOrder()).isEqualTo(YES);

        assertThat(response.getData().getFinalOrder().getEnableApplicant1SolicitorSwitchToSoleFo()).isNotNull();
        assertThat(response.getData().getFinalOrder().getEnableApplicant1SolicitorSwitchToSoleFo()).isEqualTo(YES);

        assertThat(response.getData().getFinalOrder().getEnableApplicant2SolicitorSwitchToSoleFo()).isNull();

        verify(notificationDispatcher).send(applicant1CanSwitchToSoleFinalOrderNotification, caseData, details.getId());
        verifyNoInteractions(applicant2CanSwitchToSoleFinalOrderNotification);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSenNotificationToApplicant2WhenApplicant1FinalOrderIsOverdue() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(respondent());
        caseData.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderSubmitted(LocalDate.now().minusDays(15).atStartOfDay())
            .applicant2SubmittedFinalOrder(YES)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemNotifyJointApplicantCanSwitchToSoleFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getApplication().getJointApplicantNotifiedCanSwitchToSoleFinalOrder()).isEqualTo(YES);

        assertThat(response.getData().getFinalOrder().getEnableApplicant2SolicitorSwitchToSoleFo()).isNotNull();
        assertThat(response.getData().getFinalOrder().getEnableApplicant2SolicitorSwitchToSoleFo()).isEqualTo(YES);

        assertThat(response.getData().getFinalOrder().getEnableApplicant1SolicitorSwitchToSoleFo()).isNull();

        verify(notificationDispatcher).send(applicant2CanSwitchToSoleFinalOrderNotification, caseData, details.getId());
        verifyNoInteractions(applicant1CanSwitchToSoleFinalOrderNotification);
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
