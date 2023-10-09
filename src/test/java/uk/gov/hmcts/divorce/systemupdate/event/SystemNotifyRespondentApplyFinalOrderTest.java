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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.RespondentApplyForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyRespondentApplyFinalOrder.SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(SpringExtension.class)
class SystemNotifyRespondentApplyFinalOrderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SystemNotifyRespondentApplyFinalOrder systemNotifyRespondentApplyFinalOrder;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private RespondentApplyForFinalOrderNotification respondentApplyForFinalOrderNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemNotifyRespondentApplyFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER);
    }

    @Test
    void shouldSetFinalOrderReminderSentApplicant2ToYes() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemNotifyRespondentApplyFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getFinalOrderReminderSentApplicant2()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSendNotification() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("auth header");

        systemNotifyRespondentApplyFinalOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(respondentApplyForFinalOrderNotification, caseData, details.getId());
    }
}
