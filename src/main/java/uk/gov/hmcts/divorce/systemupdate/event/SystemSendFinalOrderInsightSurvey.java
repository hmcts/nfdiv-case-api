package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.FinalOrderInsightSurveyNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderInsightSurveyInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService.CASE_ALREADY_PROCESSED_ERROR;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemSendFinalOrderInsightSurvey implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY = "system-send-final-order-insight-survey";
    public static final String CASE_NOT_YET_ELIGIBLE_FOR_INSIGHT_SURVEY_ERROR =
        "The case is not yet eligible for a final order insight survey.";

    private final NotificationDispatcher notificationDispatcher;
    private final FinalOrderInsightSurveyNotification finalOrderInsightSurveyNotification;
    private static final int MAX_NOTIFICATIONS_TO_SEND = FinalOrderInsightSurveyInvite.BY_STAGE.size();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY)
            .forState(FinalOrderComplete)
            .name("Insight Team Survey Invite")
            .description("Send Insight Team Survey Invitation or Reminder")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        final long caseId = details.getId();
        final CaseData caseData = details.getData();
        final int notificationsSent = caseData.getFinalOrder().getFinalOrderInsightSurveyStage();

        log.info("{} about to submit callback invoked for case: {}", SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY, caseId);

        if (notificationsSent >= MAX_NOTIFICATIONS_TO_SEND) {
            log.error("{} errored as case has already been processed: {}", SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY, caseId);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(CASE_ALREADY_PROCESSED_ERROR))
                .build();
        }

        final FinalOrderInsightSurveyInvite inviteStage = FinalOrderInsightSurveyInvite.BY_STAGE.get(notificationsSent);
        final LocalDate earliestNotificationDate =
            caseData.getFinalOrder().getGrantedDate().toLocalDate().plusDays(inviteStage.getDaysAfterGrantedDate());

        if (earliestNotificationDate.isAfter(LocalDate.now())) {
            log.error("{} errored as case is not yet eligible to be processed: {}", SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY, caseId);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(CASE_NOT_YET_ELIGIBLE_FOR_INSIGHT_SURVEY_ERROR))
                .build();
        }

        caseData.getFinalOrder().setFinalOrderInsightSurveyStage(notificationsSent + 1);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        final CaseData caseData = details.getData();
        final long caseId = details.getId();

        log.info("{} submitted callback invoked CaseID: {}", SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY, caseId);

        notificationDispatcher.send(finalOrderInsightSurveyNotification, caseData, caseId);

        return SubmittedCallbackResponse.builder().build();
    }
}
