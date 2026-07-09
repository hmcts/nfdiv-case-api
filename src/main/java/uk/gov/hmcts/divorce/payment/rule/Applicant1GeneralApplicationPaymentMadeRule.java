package uk.gov.hmcts.divorce.payment.rule;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.CitizenGeneralApplicationPaymentMade.CITIZEN_GENERAL_APPLICATION_PAYMENT;

@Component
public class Applicant1GeneralApplicationPaymentMadeRule implements PaymentMadeRule {

    @Override
    public boolean matches(State state, String serviceRequestRef, CaseData data) {
        return Optional.ofNullable(data.getApplicant1().getGeneralAppPayments())
            .stream()
            .flatMap(List::stream)
            .map(ListValue::getValue)
            .filter(Objects::nonNull)
            .map(Payment::getServiceRequestReference)
            .anyMatch(serviceRequestRef::equals);
    }

    @Override
    public String paymentMadeEvent() {
        return CITIZEN_GENERAL_APPLICATION_PAYMENT;
    }

    @Override
    public List<ListValue<Payment>> getPayments(CaseData data) {
        return data.getApplicant1().getGeneralAppPayments();
    }
}
