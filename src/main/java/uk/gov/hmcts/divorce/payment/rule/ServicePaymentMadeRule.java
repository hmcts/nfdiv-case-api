package uk.gov.hmcts.divorce.payment.rule;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.CitizenServicePaymentMade.CITIZEN_SERVICE_PAYMENT;

@Component
public class ServicePaymentMadeRule implements PaymentMadeRule {

    @Override
    public boolean matches(State state, String serviceRequestRef, CaseData data) {
        return State.AwaitingServicePayment.equals(state)
            && Optional.ofNullable(data.getAlternativeService())
            .map(AlternativeService::getServicePaymentFee)
            .map(FeeDetails::getServiceRequestReference)
            .filter(serviceRequestRef::equals)
            .isPresent();
    }

    @Override
    public String paymentMadeEvent() {
        return CITIZEN_SERVICE_PAYMENT;
    }

    @Override
    public List<ListValue<Payment>> getPayments(CaseData data) {
        return data.getAlternativeService().getServicePayments();
    }
}
