package uk.gov.hmcts.divorce.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.notification.SwitchedToSoleFoNotification;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@RequiredArgsConstructor
@Slf4j
@Component
public class SwitchedToSoleFinalOrderOffline implements CCDConfig<CaseData, State, UserRole> {

    public static final String SWITCH_TO_SOLE_FO_OFFLINE = "switch-to-sole-fo-offline";

    private final SwitchToSoleService switchToSoleService;

    private final NotificationDispatcher notificationDispatcher;

    private final SwitchedToSoleFoNotification switchedToSoleFoNotification;

    private final GeneralReferralService generalReferralService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SWITCH_TO_SOLE_FO_OFFLINE)
            .forState(FinalOrderRequested)
            .name("Switched to sole FO via D36")
            .description("Switched to sole FO via D36")
            .grant(CREATE_READ_UPDATE, CREATOR, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .retries(0)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        Long caseId = details.getId();
        log.info("Switched To Sole FO Offline aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData caseData = details.getData();

        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getLabelContent().setApplicationType(SOLE_APPLICATION);
        caseData.getFinalOrder().setFinalOrderSwitchedToSole(YES);

        // triggered by system update user coming from Offline Document Verified
        if (OfflineWhoApplying.APPLICANT_2.equals(caseData.getFinalOrder().getD36WhoApplying())) {
            // swap data prior to swapping roles.  If data swap fails, aboutToSubmit fails without triggering role swap in IDAM.
            switchToSoleService.switchApplicantData(caseData);
            if (!caseData.getApplication().isPaperCase()) {
                log.info("Request made via paper to switch to sole for online case id: {}", caseId);
                switchToSoleService.switchUserRoles(caseData, caseId);
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        Long caseId = details.getId();
        CaseData caseData = details.getData();

        log.info("SWITCH_TO_SOLE_FO_OFFLINE submitted callback invoked for case id: {}", caseId);

        notificationDispatcher.send(switchedToSoleFoNotification, caseData, caseId);

        generalReferralService.caseWorkerGeneralReferral(details);

        return SubmittedCallbackResponse.builder().build();
    }
}
