package uk.gov.hmcts.divorce.payment.rule;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;

@Component
public class FinalOrderPaymentMadeRule implements PaymentMadeRule {

    @Override
    public boolean matches(State state, String serviceRequestRef, CaseData data) {
        return State.AwaitingFinalOrderPayment.equals(state)
            && Optional.ofNullable(data.getFinalOrder())
            .map(FinalOrder::getApplicant2FinalOrderFeeServiceRequestReference)
            .filter(serviceRequestRef::equals)
            .isPresent();
    }

    @Override
    public String paymentMadeEvent() {
        return RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
    }

    @Override
    public List<ListValue<Payment>> getPayments(CaseData data) {
        return data.getFinalOrder().getFinalOrderPayments();
    }
}
