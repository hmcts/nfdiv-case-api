package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CitizenFinalOrderDelayReason implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_FINAL_ORDER_DELAY_REASON = "citizen-final-order-delay-reason";

    @Autowired
    private Applicant1AppliedForFinalOrderNotification applicant1AppliedForFinalOrderNotification;

    @Autowired
    private Applicant2AppliedForFinalOrderNotification applicant2AppliedForFinalOrderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_FINAL_ORDER_DELAY_REASON)
            .forState(FinalOrderOverdue)
            .name("Citizen FO delay reason")
            .description("Citizen final order delay reason")
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen final order delay reason about to submit callback invoked for Case Id: {}", details.getId());

        CaseData data = details.getData();
        State endState = FinalOrderRequested;

        if (data.isWelshApplication()) {
            data.getApplication().setWelshPreviousState(endState);
            endState = WelshTranslationReview;
            log.info("State set to WelshTranslationReview, WelshPreviousState set to {}, CaseID {}",
                data.getApplication().getWelshPreviousState(), details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(endState)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("Citizen final order delay reason submitted callback invoked for Case Id: {}", details.getId());

        if (ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), details.getId())) {
            notificationDispatcher.send(applicant1AppliedForFinalOrderNotification, details.getData(), details.getId());
        } else {
            notificationDispatcher.send(applicant2AppliedForFinalOrderNotification, details.getData(), details.getId());
        }

        return SubmittedCallbackResponse.builder().build();
    }
}

