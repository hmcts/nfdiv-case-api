package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.Payment;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.IN_PROGRESS;

@Slf4j
@Component
public class CitizenSubmitApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SUBMIT = "citizen-submit-application";

    @Autowired
    private PaymentService paymentService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_SUBMIT)
            .forStates(Draft, AwaitingPayment)
            .name("Applicant Statement of Truth")
            .description("The applicant confirms SOT")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN, CASEWORKER_DIVORCE_COURTADMIN, CASEWORKER_DIVORCE_COURTADMIN_BETA)
            .grant(READ, CASEWORKER_DIVORCE_SUPERUSER)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Submit application about to submit callback invoked");

        CaseData data = details.getData();
        CaseData caseDataCopy = data.toBuilder().build();

        log.info("Validating case data");
        final List<String> validationErrors = AwaitingPayment.validate(caseDataCopy);

        if (!validationErrors.isEmpty()) {
            log.info("Validation errors: ");
            for (String error : validationErrors) {
                log.info(error);
            }

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseDataCopy)
                .errors(validationErrors)
                .state(Draft)
                .build();
        }

        State state;
        if (caseDataCopy.getHelpWithFeesNeedHelp().toBoolean()) {
            state = AwaitingHWFDecision;
        } else {
            OrderSummary orderSummary = paymentService.getOrderSummary();
            caseDataCopy.setApplicationFeeOrderSummary(orderSummary);

            if (data.getPayments() == null || data.getPayments().isEmpty()) {
                ListValue<Payment> paymentListValue = createPendingPayment(orderSummary.getPaymentTotal());
                caseDataCopy.setPayments(singletonList(paymentListValue));
            }

            state = AwaitingPayment;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(state)
            .build();
    }

    private ListValue<Payment> createPendingPayment(String paymentTotal) {
        Payment payment = Payment
            .builder()
            .paymentAmount(Integer.valueOf(paymentTotal))
            .paymentStatus(IN_PROGRESS)
            .build();

        return ListValue
            .<Payment>builder()
            .value(payment)
            .id(UUID.randomUUID().toString())
            .build();
    }
}

