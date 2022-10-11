package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.Applicant1JointFinalOrderOverdueNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2JointFinalOrderOverdueNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemRemindJointFinalOrderOverdue implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_JOINT_FINAL_ORDER_OVERDUE = "system-joint-final-order-overdue";

    @Autowired
    private Applicant1JointFinalOrderOverdueNotification applicant1Notification;

    @Autowired
    private Applicant2JointFinalOrderOverdueNotification applicant2Notification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_JOINT_FINAL_ORDER_OVERDUE)
            .forStateTransition(AwaitingFinalOrder, FinalOrderOverdue)
            .name("Joint Final Order Overdue")
            .description("Reminder when joint final order is overdue")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();

        if (YesOrNo.YES.equals(data.getFinalOrder().getApplicant1AppliedForFinalOrderFirst())) {
            notificationDispatcher.send(applicant2Notification, data, details.getId());
        } else {
            notificationDispatcher.send(applicant1Notification, data, details.getId());
        }

        data.getApplication().setApplicantsRemindedJointFinalOrderOverdue(YesOrNo.YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
