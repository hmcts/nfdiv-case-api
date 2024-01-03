package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.notification.AwaitingConditionalOrderReminderNotification;
import uk.gov.hmcts.divorce.common.notification.ConditionalOrderPendingReminderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicantsApplyForCOrder.SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
class SystemRemindApplicantsApplyForCOrderTest {

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private AwaitingConditionalOrderReminderNotification awaitingConditionalOrderReminderNotification;

    @Mock
    private ConditionalOrderPendingReminderNotification conditionalOrderPendingReminderNotification;

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
    void shouldSendCOReminderNotificationAndSetSentNotificationFlagToYesWhenAboutToSubmitCalledForAwaitingConditionalOrder() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingConditionalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        final var response = systemRemindApplicantsApplyForCOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(awaitingConditionalOrderReminderNotification, caseData, details.getId());

        assertThat(response.getData().getApplication().getApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YES);
    }

    @Test
    void shouldSendCOPendingReminderNotificationAndSetSentNotificationFlagToYesWhenAboutToSubmitCalledForConditionalOrderDrafted() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(ConditionalOrderDrafted).id(TEST_CASE_ID).data(caseData)
            .build();

        final var response = systemRemindApplicantsApplyForCOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(awaitingConditionalOrderReminderNotification, caseData, details.getId());

        assertThat(response.getData().getApplication().getApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YES);
    }

    @Test
    void shouldSendCOPendingReminderNotificationAndSetSentNotificationFlagToYesWhenAboutToSubmitCalledForOtherState() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID).data(caseData)
            .build();

        final var response = systemRemindApplicantsApplyForCOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(conditionalOrderPendingReminderNotification, caseData, details.getId());

        assertThat(response.getData().getApplication().getApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YES);
    }
}
