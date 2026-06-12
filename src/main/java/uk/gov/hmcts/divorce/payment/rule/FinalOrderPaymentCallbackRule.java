package uk.gov.hmcts.divorce.payment.rule;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;

@Component
public class FinalOrderPaymentCallbackRule implements PaymentCallbackRule {

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
    public UpdateSuccessfulPaymentStatus updatePaymentStatusTask() {
        return new UpdateSuccessfulPaymentStatus(
            details -> details.getData().getFinalOrder().getFinalOrderPayments()
        );
    }
}
