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
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderInsightSurveyInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSendFinalOrderInsightSurvey.SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService.CASE_ALREADY_PROCESSED_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;

@ExtendWith(SpringExtension.class)
class SystemSendFinalOrderInsightSurveyTest {

    @Mock
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemSendFinalOrderInsightSurvey systemSendFinalOrderInsightSurvey;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemSendFinalOrderInsightSurvey.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY);
    }

    @Test
    void shouldErrorWhenTheCaseHasAlreadyBeenProcessedMaximumTimes() {
        final CaseData caseData = caseDataWithOrderSummary();
        caseData.getFinalOrder().setFinalOrderInsightSurveyStage(FinalOrderInsightSurveyInvite.BY_STAGE.size());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.FinalOrderComplete);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemSendFinalOrderInsightSurvey.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly(CASE_ALREADY_PROCESSED_ERROR);
    }

    @Test
    void shouldIncrementNotificationCounter() {
        final CaseData caseData = caseDataWithOrderSummary();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.FinalOrderComplete);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemSendFinalOrderInsightSurvey.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getFinalOrderInsightSurveyStage()).isEqualTo(1);
    }

    @Test
    void shouldSendNotificationWhenCaseDataIsValid() {
        final CaseData caseData = caseDataWithOrderSummary();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.FinalOrderComplete);

        systemSendFinalOrderInsightSurvey.submitted(details, details);

        verify(notificationDispatcher).send(finalOrderGrantedNotification, details.getData(), details.getId());
    }
}
