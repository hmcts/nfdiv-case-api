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
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
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
    private JointApplicantCanSwitchToSoleNotification notification;

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
        Long caseId = details.getId();

        ConditionalOrder conditionalOrder = data.getConditionalOrder();

        ConditionalOrderQuestions coQuestionsApp1 = conditionalOrder.getConditionalOrderApplicant1Questions();
        ConditionalOrderQuestions coQuestionsApp2 = conditionalOrder.getConditionalOrderApplicant2Questions();

        if (coQuestionsApp1 != null && YES.equals(coQuestionsApp1.getIsSubmitted())) {
            notification.sendToApplicant1(data, caseId);
            data.getApplication().setJointApplicantNotifiedCanSwitchToSole(YES);
        } else if (coQuestionsApp2 != null && YES.equals(coQuestionsApp2.getIsSubmitted())) {
            notification.sendToApplicant2(data, caseId);
            data.getApplication().setJointApplicantNotifiedCanSwitchToSole(YES);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
