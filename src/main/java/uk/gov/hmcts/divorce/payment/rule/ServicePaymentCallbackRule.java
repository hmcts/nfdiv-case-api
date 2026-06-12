package uk.gov.hmcts.divorce.payment.rule;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.CitizenServicePaymentMade.CITIZEN_SERVICE_PAYMENT;

@Component
public class ServicePaymentCallbackRule implements PaymentCallbackRule {

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
    public UpdateSuccessfulPaymentStatus updatePaymentStatusTask() {
        return new UpdateSuccessfulPaymentStatus(
            details -> details.getData().getAlternativeService().getServicePayments()
        );
    }
}
