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
import uk.gov.hmcts.divorce.common.notification.Applicant1RemindAwaitingJointFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2RemindAwaitingJointFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindAwaitingJointFinalOrder.SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
class SystemRemindAwaitingJointFinalOrderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Applicant1RemindAwaitingJointFinalOrderNotification applicant1Notification;

    @Mock
    private Applicant2RemindAwaitingJointFinalOrderNotification applicant2Notification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemRemindAwaitingJointFinalOrder systemRemindAwaitingJointFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemRemindAwaitingJointFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER);
    }

    @Test
    void shouldSendNotificationToApplicant2WhenApplicant2HasNotYetSubmittedJointFO() {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.setFinalOrder(FinalOrder.builder()
                .applicant1AppliedForFinalOrderFirst(YES)
                .applicant2AppliedForFinalOrderFirst(NO)
            .build());

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingJointFinalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final var response = systemRemindAwaitingJointFinalOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(applicant2Notification, caseData, details.getId());

        verifyNoMoreInteractions(notificationDispatcher);

        assertThat(response.getData().getApplication().getApplicantsRemindedAwaitingJointFinalOrder()).isEqualTo(YES);
    }

    @Test
    void shouldSendNotificationToApplicant1WhenApplicant1HasNotYetSubmittedJointFO() {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant1AppliedForFinalOrderFirst(NO)
            .applicant2AppliedForFinalOrderFirst(YES)
            .build());

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingJointFinalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final var response = systemRemindAwaitingJointFinalOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(applicant1Notification, caseData, details.getId());

        verifyNoMoreInteractions(notificationDispatcher);

        assertThat(response.getData().getApplication().getApplicantsRemindedAwaitingJointFinalOrder()).isEqualTo(YES);
    }
}
