package uk.gov.hmcts.divorce.payment.rule;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;

@Component
public class ApplicationPaymentMadeRule implements PaymentMadeRule {

    @Override
    public boolean matches(State state, String serviceRequestRef, CaseData data) {
        return State.AwaitingPayment.equals(state)
            && Optional.ofNullable(data.getApplication())
            .map(Application::getApplicationFeeServiceRequestReference)
            .filter(serviceRequestRef::equals)
            .isPresent();
    }

    @Override
    public String paymentMadeEvent() {
        return CITIZEN_PAYMENT_MADE;
    }

    @Override
    public UpdateSuccessfulPaymentStatus updatePaymentStatusTask() {
        return new UpdateSuccessfulPaymentStatus(
            details -> details.getData().getApplication().getApplicationPayments()
        );
    }
}
