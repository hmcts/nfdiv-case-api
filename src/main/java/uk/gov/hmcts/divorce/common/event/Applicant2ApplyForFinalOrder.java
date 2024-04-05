package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.Applicant2ApplyForFinalOrderDetails;
import uk.gov.hmcts.divorce.common.event.page.Applicant2FinalOrderExplainTheDelay;
import uk.gov.hmcts.divorce.common.notification.Applicant2AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.FinalOrderRequestedNotification;
import uk.gov.hmcts.divorce.common.service.ApplyForFinalOrderService;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class Applicant2ApplyForFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT2_FINAL_ORDER_REQUESTED = "applicant2-final-order-requested";

    public static final String APPLICANT2_APPLY_FOR_FINAL_ORDER = "Apply for final order";

    @Autowired
    private Applicant2AppliedForFinalOrderNotification applicant2AppliedForFinalOrderNotification;

    @Autowired
    private FinalOrderRequestedNotification finalOrderRequestedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private ApplyForFinalOrderService applyForFinalOrderService;

    @Autowired
    private GeneralReferralService generalReferralService;

    private static final List<CcdPageConfiguration> pages = List.of(
        new Applicant2ApplyForFinalOrderDetails(),
        new Applicant2FinalOrderExplainTheDelay()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(APPLICANT2_FINAL_ORDER_REQUESTED)
            .forStates(AwaitingFinalOrder, AwaitingJointFinalOrder)
            .name(APPLICANT2_APPLY_FOR_FINAL_ORDER)
            .description(APPLICANT2_APPLY_FOR_FINAL_ORDER)
            .showCondition("applicationType=\"jointApplication\" AND doesApplicant2WantToApplyForFinalOrder!=\"Yes\"")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, APPLICANT_2, APPLICANT_2_SOLICITOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                APPLICANT_1_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Applicant2 Apply For Final Order event about to submit callback invoked for Case Id: {}", details.getId());
        CaseData data = details.getData();

        data.getApplication().setPreviousState(beforeDetails.getState());

        final List<String> errors = applyForFinalOrderService.validateApplyForFinalOrder(data, true);
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(errors)
                .build();
        }

        CaseDetails<CaseData, State> updatedDetails = applyForFinalOrderService.applyForFinalOrderAsApplicant2(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedDetails.getData())
            .state(updatedDetails.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("Applicant2 Apply For Final Order event submitted callback invoked for Case Id: {}", details.getId());

        final CaseData data = details.getData();
        final State previousState = data.getApplication().getPreviousState();

        if (AwaitingFinalOrder.equals(previousState)) {
            log.info(
                "Sending Applicant 2 Applied For Final Order Notification for Case Id: {}",
                details.getId()
            );
            notificationDispatcher.send(applicant2AppliedForFinalOrderNotification, details.getData(), details.getId());
        }

        if (FinalOrderRequested.equals(details.getState())
            || WelshTranslationReview.equals(details.getState()) && FinalOrderRequested.equals(
            details.getData().getApplication().getWelshPreviousState())) {
            log.info(
                "Sending Applicant2 Apply for Final Order notifications as case in FinalOrderRequested state for Case Id: {}",
                details.getId()
            );
            notificationDispatcher.send(finalOrderRequestedNotification, details.getData(), details.getId());
        }

        var isRespondentOnSoleCase = data.getApplicationType().isSole();
        if (!isRespondentOnSoleCase) {
            generalReferralService.caseWorkerGeneralReferral(details);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
