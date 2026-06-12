package uk.gov.hmcts.divorce.payment.rule;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.CitizenGeneralApplicationPaymentMade.CITIZEN_GENERAL_APPLICATION_PAYMENT;

@Component
public class Applicant1GeneralApplicationPaymentCallbackRule implements PaymentCallbackRule {

    @Override
    public boolean matches(State state, String serviceRequestRef, CaseData data) {
        return Optional.ofNullable(data.getApplicant1().getGeneralAppServiceRequest())
            .filter(serviceRequestRef::equals)
            .isPresent();
    }

    @Override
    public String paymentMadeEvent() {
        return CITIZEN_GENERAL_APPLICATION_PAYMENT;
    }

    @Override
    public UpdateSuccessfulPaymentStatus updatePaymentStatusTask() {
        return new UpdateSuccessfulPaymentStatus(
            details -> details.getData().getApplicant1().getGeneralAppPayments()
        );
    }
}
