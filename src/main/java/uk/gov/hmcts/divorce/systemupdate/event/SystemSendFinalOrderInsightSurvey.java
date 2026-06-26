package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService.CASE_ALREADY_PROCESSED_ERROR;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemSendFinalOrderInsightSurvey implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY = "system-send-final-order-insight-survey";

    private final NotificationDispatcher notificationDispatcher;
    private final FinalOrderGrantedNotification finalOrderGrantedNotification;
    private static final int MAX_NOTIFICATIONS_TO_SEND = 3;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY)
            .forState(FinalOrderComplete)
            .name("Send Insight Team Feedback Survey")
            .description("Insight Team Feedback Survey or Reminder Sent after Final Order Granted")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE));
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

        notificationDispatcher.send(finalOrderGrantedNotification, caseData, caseId);

        return SubmittedCallbackResponse.builder().build();
    }
}
