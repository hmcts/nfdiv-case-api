package uk.gov.hmcts.divorce.payment.rule;

import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

public interface PaymentMadeRule {
    boolean matches(State state, String serviceRequestRef, CaseData data);

    String paymentMadeEvent();

    List<ListValue<Payment>> getPayments(CaseData data);
}
