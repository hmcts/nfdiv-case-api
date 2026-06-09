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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.HearingReminderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Hearing;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSendHearingReminder.SYSTEM_SEND_HEARING_REMINDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService.CASE_ALREADY_PROCESSED_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
class SystemSendHearingRemindersTest {

    @Mock
    private HearingReminderNotification hearingReminderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemSendHearingReminder sendHearingReminder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        sendHearingReminder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_SEND_HEARING_REMINDER);
    }

    @Test
    void shouldErrorWhenTheCaseHasAlreadyBeenProcessed() {
        final CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.PendingHearingOutcome);
        caseData.setHearing(
            Hearing.builder()
                .hasHearingReminderBeenSent(YesOrNo.YES)
                .build()
        );

        final AboutToStartOrSubmitResponse<CaseData, State> response = sendHearingReminder.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly(CASE_ALREADY_PROCESSED_ERROR);
    }

    @Test
    void shouldSetNotificationSentFlag() {
        final CaseData caseData = caseDataWithOrderSummary();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.PendingHearingOutcome);

        final AboutToStartOrSubmitResponse<CaseData, State> response = sendHearingReminder.aboutToSubmit(details, details);

        assertThat(response.getData().getHearing().getHasHearingReminderBeenSent()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSendNotificationWhenCaseDataIsValid() {
        final CaseData caseData = caseDataWithOrderSummary();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.PendingHearingOutcome);

        sendHearingReminder.submitted(details, details);

        verify(notificationDispatcher).send(hearingReminderNotification, details.getData(), details.getId());
    }
}
