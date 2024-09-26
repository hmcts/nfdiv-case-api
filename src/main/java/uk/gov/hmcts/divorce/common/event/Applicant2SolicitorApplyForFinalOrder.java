package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolApplyForFinalOrderDetails;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolFinalOrderExplainWhyNeedToApply;
import uk.gov.hmcts.divorce.common.notification.Applicant2SolicitorAppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.service.ApplyForFinalOrderService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPageForApplicant2SolFinalOrder;
import uk.gov.hmcts.divorce.solicitor.event.page.SolFinalOrderPayAccount;
import uk.gov.hmcts.divorce.solicitor.event.page.SolFinalOrderPayment;
import uk.gov.hmcts.divorce.solicitor.event.page.SolFinalOrderPaymentSummary;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Component
@Slf4j
public class Applicant2SolicitorApplyForFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String FINAL_ORDER_REQUESTED_APP2_SOL = "final-order-requested-app2-sol";

    public static final String APPLY_FOR_FINAL_ORDER = "Apply for final order";

    @Autowired
    private Applicant2SolicitorAppliedForFinalOrderNotification applicant2SolicitorAppliedForFinalOrderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private ApplyForFinalOrderService applyForFinalOrderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SolFinalOrderPayment solFinalOrderPayment;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<CcdPageConfiguration> pages = List.of(
            new Applicant2SolApplyForFinalOrderDetails(),
            new Applicant2SolFinalOrderExplainWhyNeedToApply(),
            solFinalOrderPayment,
            new HelpWithFeesPageForApplicant2SolFinalOrder(),
            new SolFinalOrderPayAccount(),
            new SolFinalOrderPaymentSummary()
        );

        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(FINAL_ORDER_REQUESTED_APP2_SOL)
            .forStates(AwaitingFinalOrder, AwaitingFinalOrderPayment)
            .name(APPLY_FOR_FINAL_ORDER)
            .description(APPLY_FOR_FINAL_ORDER)
            .showCondition("applicationType=\"soleApplication\" AND finalOrderReminderSentApplicant2=\"Yes\"")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .endButtonLabel("Submit Application")
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                APPLICANT_1_SOLICITOR,
                JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", FINAL_ORDER_REQUESTED_APP2_SOL, details.getId());

        CaseData data = details.getData();
        log.info("Retrieving order summary");
        final OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE);

        data.getFinalOrder().setApplicant2SolFinalOrderFeeOrderSummary(orderSummary);

        data.getFinalOrder().setApplicant2SolFinalOrderFeeInPounds(
            NumberFormat.getNumberInstance().format(
                new BigDecimal(orderSummary.getPaymentTotal()).movePointLeft(2)
            )
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", FINAL_ORDER_REQUESTED_APP2_SOL, details.getId());

        CaseDetails<CaseData, State> updatedDetails = applyForFinalOrderService.applyForFinalOrderAsApplicant2Sol(details);
        CaseData updatedData = updatedDetails.getData();
        var updatedFo = updatedData.getFinalOrder();

        final var finalOrderFeeOrderSummary = updatedFo.getApplicant2SolFinalOrderFeeOrderSummary();

        if (updatedFo.isSolicitorPaymentMethodPba()) {
            final Optional<String> pbaNumber = Optional.ofNullable(updatedFo.getFinalOrderPbaNumbers())
                .map(dynamicList -> dynamicList.getValue().getLabel());
            if (pbaNumber.isPresent()) {
                final PbaResponse response = paymentService.processPbaPayment(
                    updatedData,
                    updatedDetails.getId(),
                    updatedData.getApplicant2().getSolicitor(),
                    pbaNumber.get(),
                    updatedFo.getApplicant2SolFinalOrderFeeOrderSummary(),
                    updatedFo.getApplicant2SolFinalOrderFeeAccountReference()
                );

                if (response.getHttpStatus() == CREATED) {
                    updatedFo.updateFinalOrderWithApp2SolPaymentDetails(
                        finalOrderFeeOrderSummary,
                        updatedData,
                        response.getPaymentReference(),
                        pbaNumber.get()
                    );
                } else {
                    return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                        .data(details.getData())
                        .errors(singletonList(response.getErrorMessage()))
                        .build();
                }
            } else {
                log.error(
                    "PBA number not present when payment method is 'Solicitor fee account (PBA)' for CaseId: {}",
                    details.getId());

                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(updatedData)
                    .errors(singletonList("PBA number not present when payment method is 'Solicitor fee account (PBA)'"))
                    .build();
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedData)
            .state(updatedDetails.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} submitted callback invoked for Case Id: {}", FINAL_ORDER_REQUESTED_APP2_SOL, details.getId());

        log.info("Sending Applicant 2 Applied For Final Order Notification for Case Id: {}", details.getId());
        notificationDispatcher.send(applicant2SolicitorAppliedForFinalOrderNotification, details.getData(), details.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}
