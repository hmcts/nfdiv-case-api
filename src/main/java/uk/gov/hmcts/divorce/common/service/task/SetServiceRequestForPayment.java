package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.payment.PaymentService;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;

@Component
@Slf4j
@RequiredArgsConstructor
public class SetServiceRequestForPayment implements CaseTask {

    private final PaymentService paymentService;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> details) {

        final State state = details.getState();

        if (AwaitingPayment.equals(state)) {
            setServiceRequestForApplicationPayment(details);
        } else if (AwaitingFinalOrderPayment.equals(state)) {
            setServiceRequestForFinalOrderPayment(details);
        }

        return details;
    }

    private void setServiceRequestForApplicationPayment(CaseDetails<CaseData, State> details) {
        final CaseData data = details.getData();
        final Application application = data.getApplication();
        final OrderSummary orderSummary = application.getApplicationFeeOrderSummary();

        final String serviceRequestReference = createServiceRequest(details, data.getApplicant1(), orderSummary);
        application.setApplicationFeeServiceRequestReference(serviceRequestReference);
    }

    private void setServiceRequestForFinalOrderPayment(CaseDetails<CaseData, State> details) {
        final CaseData data = details.getData();
        final FinalOrder finalOrder = data.getFinalOrder();
        final OrderSummary orderSummary = finalOrder.getApplicant2FinalOrderFeeOrderSummary();

        final String serviceRequestReference = createServiceRequest(
            details, data.getApplicant2(), orderSummary
        );
        finalOrder.setApplicant2FinalOrderFeeServiceRequestReference(serviceRequestReference);
    }

    private String createServiceRequest(final CaseDetails<CaseData, State> details, Applicant responsibleParty, OrderSummary orderSummary) {
        return paymentService.createServiceRequestReference(
            details.getData().getCitizenPaymentCallbackUrl(),
            details.getId(),
            responsibleParty.getFullName(),
            orderSummary
        );
    }
}
