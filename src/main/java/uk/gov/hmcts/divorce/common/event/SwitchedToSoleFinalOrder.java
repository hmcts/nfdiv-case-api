package uk.gov.hmcts.divorce.common.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.FinalOrderExplainTheDelay;
import uk.gov.hmcts.divorce.common.notification.SwitchedToSoleFoNotification;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
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
public class SwitchedToSoleFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SWITCH_TO_SOLE_FO = "switch-to-sole-fo";

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest httpServletRequest;

    private final SwitchToSoleService switchToSoleService;

    private final NotificationDispatcher notificationDispatcher;

    private final SwitchedToSoleFoNotification switchedToSoleFoNotification;

    private final GeneralReferralService generalReferralService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        new FinalOrderExplainTheDelay().addTo(pageBuilder);
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(
            configBuilder
                .event(SWITCH_TO_SOLE_FO)
                .forStateTransition(AwaitingJointFinalOrder, FinalOrderRequested)
                .name("Switched to sole final order")
                .description("Switched to sole final order")
                .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2, SYSTEMUPDATE)
                .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
                .retries(0) //Do not allow retries - fails without error when ccdAccessService call fails, as !isApplicant2 on retry
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        Long caseId = details.getId();
        log.info("Switched To Sole FO aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData caseData = details.getData();

        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getLabelContent().setApplicationType(SOLE_APPLICATION);
        caseData.getFinalOrder().setFinalOrderSwitchedToSole(YES);

        // triggered by citizen users
        if (ccdAccessService.isApplicant2(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            log.info("Request made by applicant to switch to sole for case id: {}", caseId);
            // swap data prior to swapping roles.  If data swap fails, aboutToSubmit fails without triggering role swap in IDAM.
            switchToSoleService.switchApplicantData(caseData);
            switchToSoleService.switchUserRoles(beforeDetails.getData(), caseId);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("SWITCH_TO_SOLE_FO submitted callback invoked for case id: {}", details.getId());

        notificationDispatcher.send(switchedToSoleFoNotification, details.getData(), details.getId());

        generalReferralService.caseWorkerGeneralReferral(details);

        return SubmittedCallbackResponse.builder().build();
    }
}
