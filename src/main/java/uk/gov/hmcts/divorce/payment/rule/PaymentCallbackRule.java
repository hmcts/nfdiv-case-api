package uk.gov.hmcts.divorce.payment.rule;

import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

public interface PaymentCallbackRule {
    boolean matches(State state, String serviceRequestRef, CaseData data);

    String paymentMadeEvent();

    UpdateSuccessfulPaymentStatus updatePaymentStatusTask();
}
