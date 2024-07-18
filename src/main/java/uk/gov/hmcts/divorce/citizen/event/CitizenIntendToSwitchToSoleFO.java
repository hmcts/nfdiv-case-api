package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1IntendToSwitchToSoleFoNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2IntendToSwitchToSoleFoNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenIntendToSwitchToSoleFO implements CCDConfig<CaseData, State, UserRole> {

    public static final String INTEND_SWITCH_TO_SOLE_FO = "intend-switch-to-sole-fo";

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private Applicant1IntendToSwitchToSoleFoNotification applicant1IntendToSwitchToSoleFoNotification;

    @Autowired
    private Applicant2IntendToSwitchToSoleFoNotification applicant2IntendToSwitchToSoleFoNotification;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(INTEND_SWITCH_TO_SOLE_FO)
            .forState(AwaitingJointFinalOrder)
            .showCondition(NEVER_SHOW)
            .name("Intend to switch to sole FO")
            .description("Intend to switch to sole FO")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
                SUPER_USER,
                CASE_WORKER,
                LEGAL_ADVISOR)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final Long caseId = details.getId();
        CaseData data = details.getData();

        if (ccdAccessService.isApplicant1(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            data.getFinalOrder().setDoesApplicant1IntendToSwitchToSole(YES);
            data.getFinalOrder().setDateApplicant1DeclaredIntentionToSwitchToSoleFo(LocalDate.now(clock));
        } else if (ccdAccessService.isApplicant2(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            data.getFinalOrder().setDoesApplicant2IntendToSwitchToSole(YES);
            data.getFinalOrder().setDateApplicant2DeclaredIntentionToSwitchToSoleFo(LocalDate.now(clock));
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("INTEND_SWITCH_TO_SOLE_FO submitted-callback invoked for case id: {}", details.getId());

        final Long caseId = details.getId();
        final CaseData caseData = details.getData();

        if (ccdAccessService.isApplicant1(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            log.info("Sending applicant 1 intends to switch to sole fo notifications for case id: {} ", caseId);
            notificationDispatcher.send(applicant1IntendToSwitchToSoleFoNotification, caseData, caseId);
        } else if (ccdAccessService.isApplicant2(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            log.info("Sending applicant 2 intends to switch to sole fo notifications for case id: {} ", caseId);
            notificationDispatcher.send(applicant2IntendToSwitchToSoleFoNotification, caseData, caseId);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
