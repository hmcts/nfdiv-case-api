package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.Applicant1CanSwitchToSoleFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2CanSwitchToSoleFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemNotifyJointApplicantCanSwitchToSoleFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER
        = "system-notify-joint-applicant-switch-to-sole-final-order";

    @Autowired
    private Applicant1CanSwitchToSoleFinalOrderNotification applicant1CanSwitchToSoleFinalOrderNotification;

    @Autowired
    private Applicant2CanSwitchToSoleFinalOrderNotification applicant2CanSwitchToSoleFinalOrderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER)
            .forStates(AwaitingJointFinalOrder)
            .name("Notify Switch To Sole for FO")
            .description("Notify Joint Applicant they can switch to sole at FO stage")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        Long caseId = details.getId();

        FinalOrder finalOrder = data.getFinalOrder();

        if (finalOrder.shouldEnableSwitchToSoleFoForApplicant1()) {
            notificationDispatcher.send(applicant1CanSwitchToSoleFinalOrderNotification, data, caseId);
            data.getApplication().setJointApplicantNotifiedCanSwitchToSoleFinalOrder(YES);
            data.getFinalOrder().setEnableApplicant1SolicitorSwitchToSoleFo(YES);

        } else if (finalOrder.shouldEnableSwitchToSoleFoForApplicant2()) {
            notificationDispatcher.send(applicant2CanSwitchToSoleFinalOrderNotification, data, caseId);
            data.getApplication().setJointApplicantNotifiedCanSwitchToSoleFinalOrder(YES);
            data.getFinalOrder().setEnableApplicant2SolicitorSwitchToSoleFo(YES);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
