package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.RespondentApplyForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SystemNotifyRespondentApplyFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER = "system-notify-respondent-apply-final-order";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private RespondentApplyForFinalOrderNotification respondentApplyForFinalOrderNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER)
            .forState(AwaitingFinalOrder)
            .name("Notify respondent final order")
            .description("Notify respondent that they can make a Final Order application")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final Long caseId = details.getId();
        log.info("A period of 3 months has elapsed since applicant became eligible to apply for final order for case {}. "
            + "Triggering notificationDispatcher...", caseId);

        notificationDispatcher.send(respondentApplyForFinalOrderNotification, caseData, caseId);

        caseData.getFinalOrder().setFinalOrderReminderSentApplicant2(YES);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
