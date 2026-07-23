package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.FinalOrderInsightSurveyNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderInsightSurveyInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSendFinalOrderInsightSurvey.CASE_NOT_YET_ELIGIBLE_FOR_INSIGHT_SURVEY_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSendFinalOrderInsightSurvey.SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService.CASE_ALREADY_PROCESSED_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;

@ExtendWith(SpringExtension.class)
class SystemSendFinalOrderInsightSurveyTest {

    @Mock
    private FinalOrderInsightSurveyNotification finalOrderInsightSurveyNotification;

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
    void shouldErrorWhenTheCaseIsNotEligibleYet() {
        final CaseData caseData = caseDataWithOrderSummary();
        caseData.getFinalOrder().setFinalOrderInsightSurveyStage(FinalOrderInsightSurveyInvite.BY_STAGE.size());

        caseData.getFinalOrder().setGrantedDate(
            LocalDateTime.now().minusDays(FinalOrderInsightSurveyInvite.FIRST_NOTIFICATION.getDaysAfterGrantedDate())
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.FinalOrderComplete);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemSendFinalOrderInsightSurvey.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly(CASE_ALREADY_PROCESSED_ERROR);
    }

    @Test
    void shouldErrorWhenTheCaseHasAlreadyBeenProcessedMaximumTimes() {
        final CaseData caseData = caseDataWithOrderSummary();
        caseData.getFinalOrder().setFinalOrderInsightSurveyStage(FinalOrderInsightSurveyInvite.BY_STAGE.size());

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemSendFinalOrderInsightSurvey.aboutToSubmit(caseDetails(caseData), caseDetails(caseData));

        assertThat(response.getErrors()).containsExactly(CASE_ALREADY_PROCESSED_ERROR);
    }

    @ParameterizedTest
    @MethodSource("inviteStages")
    void shouldIncrementNotificationCounterWhenGrantedDateFallsOnEligibleBoundary(FinalOrderInsightSurveyInvite inviteStage) {
        final CaseData caseData = caseDataWithOrderSummary();
        caseData.getFinalOrder().setFinalOrderInsightSurveyStage(inviteStage.getStage());
        caseData.getFinalOrder().setGrantedDate(
            LocalDate.now().minusDays(inviteStage.getDaysAfterGrantedDate()).atTime(23, 59, 59)
        );

        final CaseDetails<CaseData, State> details = caseDetails(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemSendFinalOrderInsightSurvey.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getFinalOrderInsightSurveyStage()).isEqualTo(inviteStage.getStage() + 1);
    }

    @ParameterizedTest
    @MethodSource("inviteStages")
    void shouldErrorWhenCaseIsNotYetEligibleForCurrentInviteStage(FinalOrderInsightSurveyInvite inviteStage) {
        final CaseData caseData = caseDataWithOrderSummary();
        caseData.getFinalOrder().setFinalOrderInsightSurveyStage(inviteStage.getStage());
        caseData.getFinalOrder().setGrantedDate(
            LocalDate.now().minusDays(inviteStage.getDaysAfterGrantedDate()).plusDays(1).atStartOfDay()
        );

        final CaseDetails<CaseData, State> details = caseDetails(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemSendFinalOrderInsightSurvey.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly(CASE_NOT_YET_ELIGIBLE_FOR_INSIGHT_SURVEY_ERROR);
    }

    @Test
    void shouldSendNotificationWhenCaseDataIsValid() {
        final CaseData caseData = caseDataWithOrderSummary();
        caseData.getFinalOrder().setFinalOrderInsightSurveyStage(0);

        final CaseDetails<CaseData, State> details = caseDetails(caseData);

        systemSendFinalOrderInsightSurvey.submitted(details, details);

        verify(notificationDispatcher).send(finalOrderInsightSurveyNotification, details.getData(), details.getId());
    }

    private static Stream<FinalOrderInsightSurveyInvite> inviteStages() {
        return FinalOrderInsightSurveyInvite.BY_STAGE.stream();
    }

    private static CaseDetails<CaseData, State> caseDetails(CaseData caseData) {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.FinalOrderComplete);
        return details;
    }
}
