package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.JointApplicantCanSwitchToSoleNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemNotifyJointApplicantCanSwitchToSole implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE
        = "system-notify-joint-applicant-switch-to-sole";

    @Autowired
    private JointApplicantCanSwitchToSoleNotification jointApplicantCanSwitchToSoleNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE)
            .forStates(ConditionalOrderPending)
            .name("Notify Switch To Sole")
            .description("Notify Joint Applicant they can switch to sole")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();

        ConditionalOrder conditionalOrder = data.getConditionalOrder();

        if (conditionalOrder.getConditionalOrderApplicant1Questions() != null
            && YES.equals(conditionalOrder.getConditionalOrderApplicant1Questions().getIsSubmitted())) {
            jointApplicantCanSwitchToSoleNotification.sendToApplicant1(data, details.getId());
            data.getApplication().setJointApplicantNotifiedCanSwitchToSole(YES);
        } else if (conditionalOrder.getConditionalOrderApplicant2Questions() != null
            && YES.equals(conditionalOrder.getConditionalOrderApplicant2Questions().getIsSubmitted())) {
            jointApplicantCanSwitchToSoleNotification.sendToApplicant2(data, details.getId());
            data.getApplication().setJointApplicantNotifiedCanSwitchToSole(YES);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
