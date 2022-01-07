package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.AwaitingConditionalOrderReminderNotification;
import uk.gov.hmcts.divorce.notification.ConditionalOrderPendingReminderNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicantsApplyForCOrder.SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderQuestions;

@ExtendWith(SpringExtension.class)
class SystemRemindApplicantsApplyForCOrderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AwaitingConditionalOrderReminderNotification awaitingConditionalOrderReminderNotification;

    @Mock
    private ConditionalOrderPendingReminderNotification conditionalOrderPendingReminderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemRemindApplicantsApplyForCOrder systemRemindApplicantsApplyForCOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemRemindApplicantsApplyForCOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER);
    }

    @Test
    void shouldSendNotificationToBothApplicantsWhenStateIsAwaitingConditionalOrderAndJointApplication() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingConditionalOrder).id(1L).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final var response = systemRemindApplicantsApplyForCOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(awaitingConditionalOrderReminderNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        assertThat(response.getData().getApplication().getApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YES);
    }

    @Test
    void shouldSendNotificationToApplicant1WhenStateIsAwaitingConditionalOrderAndSoleApplication() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingConditionalOrder).id(1L).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final var response = systemRemindApplicantsApplyForCOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(awaitingConditionalOrderReminderNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        assertThat(response.getData().getApplication().getApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YES);
    }

    @Test
    void shouldSendNotificationToApplicant2WhenStateIsConditionalOrderPendingAndSubmittedDateIsSet() {
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(ConditionalOrderPending).id(1L).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final var response = systemRemindApplicantsApplyForCOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(conditionalOrderPendingReminderNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        assertThat(response.getData().getApplication().getApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YES);
    }

    @Test
    void shouldSendNotificationToApplicant1WhenStateConditionalOrderPendingAndSubmittedDateIsNotSet() {
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(ConditionalOrderPending).id(1L).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final var response = systemRemindApplicantsApplyForCOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(conditionalOrderPendingReminderNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        assertThat(response.getData().getApplication().getApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YES);
    }
}
