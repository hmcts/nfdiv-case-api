package uk.gov.hmcts.divorce.payment.rule;

import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public interface PaymentCallbackRule {
    boolean matches(State state, String serviceRequestRef, CaseData data);

    String paymentMadeEvent();

    UpdateSuccessfulPaymentStatus updatePaymentStatusTask();

    UserRole userRole();
}
