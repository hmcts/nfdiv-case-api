package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.PartnerNotAppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService.CASE_ALREADY_PROCESSED_ERROR;

@Component
@RequiredArgsConstructor
public class SystemNotifyApplicantPartnerNotAppliedForFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER = "system-notify-applicant-partner-not-applied-final-order";

    private final NotificationDispatcher notificationDispatcher;

    private final PartnerNotAppliedForFinalOrderNotification partnerNotAppliedForFinalOrderNotification;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER)
            .forStates(AwaitingJointFinalOrder)
            .name("Partner not applied for FO")
            .description("Notify Applicant that partner not applied for Final Order")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SUPER_USER)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        FinalOrder finalOrder = data.getFinalOrder();
        if (YesOrNo.YES.equals(finalOrder.getFinalOrderFirstInTimeNotifiedOtherApplicantNotApplied())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(CASE_ALREADY_PROCESSED_ERROR))
                .build();
        }

        notificationDispatcher.send(partnerNotAppliedForFinalOrderNotification, data, details.getId());

        if (!data.getApplicationType().isSole() && YES.equals(finalOrder.getApplicant1AppliedForFinalOrderFirst())) {
            finalOrder.setApplicant1CanIntendToSwitchToSoleFo(YES);
            finalOrder.setDoesApplicant1IntendToSwitchToSole(NO);
        } else if (!data.getApplicationType().isSole() && YES.equals(finalOrder.getApplicant2AppliedForFinalOrderFirst())) {
            finalOrder.setApplicant2CanIntendToSwitchToSoleFo(YES);
            finalOrder.setDoesApplicant2IntendToSwitchToSole(NO);
        }

        finalOrder.setFinalOrderFirstInTimeNotifiedOtherApplicantNotApplied(YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
