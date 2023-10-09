package uk.gov.hmcts.divorce.systemupdate.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.Applicant1CanSwitchToSoleNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2CanSwitchToSoleNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(SpringExtension.class)
class SystemNotifyJointApplicantCanSwitchToSoleTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Applicant1CanSwitchToSoleNotification applicant1CanSwitchToSoleNotification;

    @Mock
    private Applicant2CanSwitchToSoleNotification applicant2CanSwitchToSoleNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemNotifyJointApplicantCanSwitchToSole systemNotifyJointApplicantCanSwitchToSole;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemNotifyJointApplicantCanSwitchToSole.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SystemNotifyJointApplicantCanSwitchToSole.SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE);
    }

    @Test
    void shouldSenNotificationToApplicant1WhenApplicant2ConditionalOrderIsOverdue() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        caseData.setConditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(LocalDate.now().minusDays(15).atStartOfDay())
                    .isSubmitted(YES)
                    .build())
                .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemNotifyJointApplicantCanSwitchToSole.aboutToSubmit(details, details);

        assertThat(response.getData().getApplication().getJointApplicantNotifiedCanSwitchToSole()).isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getEnableSolicitorSwitchToSoleCo())
            .isNotNull();
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getEnableSolicitorSwitchToSoleCo())
            .isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getEnableSolicitorSwitchToSoleCo())
            .isNull();
        verify(notificationDispatcher).send(applicant1CanSwitchToSoleNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSenNotificationToApplicant2WhenApplicant1ConditionalOrderIsOverdue() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                .submittedDate(LocalDate.now().minusDays(15).atStartOfDay())
                .isSubmitted(YES)
                .build())
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemNotifyJointApplicantCanSwitchToSole.aboutToSubmit(details, details);

        assertThat(response.getData().getApplication().getJointApplicantNotifiedCanSwitchToSole()).isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getEnableSolicitorSwitchToSoleCo())
            .isNotNull();
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getEnableSolicitorSwitchToSoleCo())
            .isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getEnableSolicitorSwitchToSoleCo())
            .isNull();
        verify(notificationDispatcher).send(applicant2CanSwitchToSoleNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
