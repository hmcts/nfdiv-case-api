package uk.gov.hmcts.divorce.payment.rule;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.CitizenGeneralApplicationPaymentMade.CITIZEN_GENERAL_APPLICATION_PAYMENT;

@Component
public class Applicant2GeneralApplicationPaymentMadeRule implements PaymentMadeRule {

    @Override
    public boolean matches(State state, String serviceRequestRef, CaseData data) {
        return Optional.ofNullable(data.getApplicant2().getGeneralAppServiceRequest())
            .filter(serviceRequestRef::equals)
            .isPresent();
    }

    @Override
    public String paymentMadeEvent() {
        return CITIZEN_GENERAL_APPLICATION_PAYMENT;
    }

    @Override
    public List<ListValue<Payment>> getPayments(CaseData data) {
        return data.getApplicant2().getGeneralAppPayments();
    }
}
