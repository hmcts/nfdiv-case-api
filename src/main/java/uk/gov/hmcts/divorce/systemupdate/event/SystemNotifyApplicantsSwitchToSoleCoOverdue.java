package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.Applicant1CoOverdueSwitchToSoleNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2CoOverdueSwitchToSoleNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemNotifyApplicantsSwitchToSoleCoOverdue implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private Applicant1CoOverdueSwitchToSoleNotification applicant1CoOverdueSwitchToSoleNotification;

    @Autowired
    private Applicant2CoOverdueSwitchToSoleNotification applicant2CoOverdueSwitchToSoleNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    public static final String SYSTEM_CONDITIONAL_ORDER_OVERDUE = "system-co-overdue";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_CONDITIONAL_ORDER_OVERDUE)
            .forState(ConditionalOrderPending)
            .name("Notify applicant CO overdue - switch to sole")
            .description("Notify Applicant that CO is overdue and they can switch to sole")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        ConditionalOrderQuestions applicant1Co = caseData.getConditionalOrder().getConditionalOrderApplicant1Questions();

        if (applicant1Co.getIsSubmitted().toBoolean()) {
            notificationDispatcher.send(applicant1CoOverdueSwitchToSoleNotification, caseData, details.getId());
        } else {
            notificationDispatcher.send(applicant2CoOverdueSwitchToSoleNotification, caseData, details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
