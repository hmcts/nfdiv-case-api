package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.ApplicantSwitchToSoleAfterIntentionFONotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService.CASE_ALREADY_PROCESSED_ERROR;

@Component
@RequiredArgsConstructor
public class SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION
        = "system-notify-applicant-can-switch-to-sole-after-intention-fo";

    private final NotificationDispatcher notificationDispatcher;

    private final ApplicantSwitchToSoleAfterIntentionFONotification applicantSwitchToSoleAfterIntentionFONotification;

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
        FinalOrder finalOrder = data.getFinalOrder();
        boolean applicant1Notified = YesOrNo.YES.equals(finalOrder.getFinalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention());
        boolean applicant2Notified = YesOrNo.YES.equals(finalOrder.getFinalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention());
        if (applicant1Notified || applicant2Notified) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(CASE_ALREADY_PROCESSED_ERROR))
                .build();
        }

        notificationDispatcher.send(applicantSwitchToSoleAfterIntentionFONotification, data, details.getId());

        if (finalOrder.getApplicant1IntendsToSwitchToSole() != null
            && finalOrder.getApplicant1IntendsToSwitchToSole().contains(I_INTEND_TO_SWITCH_TO_SOLE)) {
            finalOrder.setFinalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(YES);
        } else if (finalOrder.getApplicant2IntendsToSwitchToSole() != null
            && finalOrder.getApplicant2IntendsToSwitchToSole().contains(I_INTEND_TO_SWITCH_TO_SOLE)) {
            finalOrder.setFinalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention(YES);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
