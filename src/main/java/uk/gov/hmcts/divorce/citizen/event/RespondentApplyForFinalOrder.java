package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.math.BigDecimal;
import java.text.NumberFormat;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRespondentFOPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Component
@Slf4j
@RequiredArgsConstructor
public class RespondentApplyForFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String RESPONDENT_APPLY_FINAL_ORDER = "respondent-apply-final-order";

    private final PaymentService paymentService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(RESPONDENT_APPLY_FINAL_ORDER)
            .forStates(AwaitingFinalOrder, AwaitingJointFinalOrder)
            .name("Respondent apply final order")
            .description("Respondent would like to apply for final order")
            .showCondition(NEVER_SHOW)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, APPLICANT_2)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, APPLICANT_2_SOLICITOR);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} About to Submit callback invoked for Case Id: {}", RESPONDENT_APPLY_FINAL_ORDER, details.getId());

        CaseData data = details.getData();
        FinalOrder finalOrder = data.getFinalOrder();
        State state = RespondentFinalOrderRequested;

        if (finalOrder.applicant2NeedsHelpWithFees()) {
            // HWF logic to be added in NFDIV-4098
        } else {
            final OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE);

            finalOrder.setApplicant2FinalOrderFeeOrderSummary(orderSummary);

            finalOrder.setApplicant2FinalOrderFeeInPounds(
                NumberFormat.getNumberInstance().format(new BigDecimal(
                    orderSummary.getPaymentTotal()).movePointLeft(2)
                )
            );

            state = AwaitingRespondentFOPayment;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }
}