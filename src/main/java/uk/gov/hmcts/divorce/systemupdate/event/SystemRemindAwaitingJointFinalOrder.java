package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.Applicant1RemindAwaitingJointFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2RemindAwaitingJointFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemRemindAwaitingJointFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER = "system-remind-awaiting-joint-final-order";

    @Autowired
    private Applicant1RemindAwaitingJointFinalOrderNotification applicant1Notification;

    @Autowired
    private Applicant2RemindAwaitingJointFinalOrderNotification applicant2Notification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER)
            .forState(AwaitingJointFinalOrder)
            .name("Remind Awaiting Joint FO")
            .description("Reminding applicants that their joint final order is awaiting")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();

        if (YES.equals(data.getFinalOrder().getApplicant1AppliedForFinalOrderFirst())) {
            notificationDispatcher.send(applicant2Notification, data, details.getId());
        } else {
            notificationDispatcher.send(applicant1Notification, data, details.getId());
        }

        data.getApplication().setApplicantsRemindedAwaitingJointFinalOrder(YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
