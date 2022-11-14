package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.ApplicantSwitchToSoleAfterIntentionFONotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION
        = "system-notify-applicant-can-switch-to-sole-after-intention-fo";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private ApplicantSwitchToSoleAfterIntentionFONotification applicantSwitchToSoleAfterIntentionFONotification;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION)
            .forStates(AwaitingJointFinalOrder)
            .name("Notify continue switch to sole")
            .description("Notify Applicant they can continue switch to sole")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();

        notificationDispatcher.send(applicantSwitchToSoleAfterIntentionFONotification, data, details.getId());

        if (data.getFinalOrder().getApplicant1IntendsToSwitchToSole() != null
            && data.getFinalOrder().getApplicant1IntendsToSwitchToSole().contains(I_INTEND_TO_SWITCH_TO_SOLE)) {
            data.getFinalOrder().setFinalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(YES);
        } else if (data.getFinalOrder().getApplicant2IntendsToSwitchToSole() != null
            && data.getFinalOrder().getApplicant2IntendsToSwitchToSole().contains(I_INTEND_TO_SWITCH_TO_SOLE)) {
            data.getFinalOrder().setFinalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention(YES);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
