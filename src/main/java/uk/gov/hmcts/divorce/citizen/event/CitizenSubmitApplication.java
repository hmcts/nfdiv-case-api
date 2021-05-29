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
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitApplicationService;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
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
    private SolicitorSubmitApplicationService solicitorSubmitApplicationService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_SUBMIT)
            .forStates(Draft, AwaitingDocuments, AwaitingPayment)
            .name("Applicant 1 Statement of Truth")
            .description("Applicant 1 confirms SOT")
            .aboutToStartCallback(this::aboutToStart)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grant(READ, CASEWORKER_DIVORCE_SUPERUSER);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Submit application about to start callback invoked");

        CaseData caseDataCopy = details.getData().toBuilder().build();
        OrderSummary orderSummary = solicitorSubmitApplicationService.getOrderSummary();
        caseDataCopy.setApplicationFeeOrderSummary(orderSummary);

        ListValue<Payment> paymentListValue = createPendingPayment(orderSummary.getPaymentTotal());
        caseDataCopy.setPayments(singletonList(paymentListValue));

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
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(AwaitingPayment)
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

