package uk.gov.hmcts.divorce.payment.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

class FinalOrderPaymentMadeRuleTest {

    private FinalOrderPaymentMadeRule rule;

    @BeforeEach
    void setup() {
        rule = new FinalOrderPaymentMadeRule();
    }

    @Test
    void returnsFalseIfStateIsNotAwaitingFinalOrderPayment() {
        final CaseData data = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .applicant2FinalOrderFeeServiceRequestReference(TEST_SERVICE_REFERENCE)
                .build())
            .build();

        final boolean result = rule.matches(State.Holding, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isFalse();
    }

    @Test
    void returnsFalseIfServiceRequestDoesNotMatch() {
        final CaseData data = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .applicant2FinalOrderFeeServiceRequestReference(TEST_SERVICE_REFERENCE)
                .build())
            .build();

        final boolean result = rule.matches(State.AwaitingFinalOrderPayment, "dummy", data);

        assertThat(result).isFalse();
    }

    @Test
    void returnsTrueIfStateIsAwaitingPaymentAndServiceRequestMatches() {
        final CaseData data = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .applicant2FinalOrderFeeServiceRequestReference(TEST_SERVICE_REFERENCE)
                .build())
            .build();

        final boolean result = rule.matches(State.AwaitingFinalOrderPayment, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isTrue();
    }

    @Test
    void returnsPaymentMadeEvent() {
        assertThat(rule.paymentMadeEvent()).isEqualTo(
            RESPONDENT_FINAL_ORDER_PAYMENT_MADE
        );
    }

    @Test
    void returnsPayments() {
        final List<ListValue<Payment>> payments = List.of(
            ListValue.<Payment>builder().value(Payment.builder().build()).build()
        );

        final CaseData data = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .finalOrderPayments(payments)
                .build())
            .build();

        assertThat(rule.getPayments(data)).isEqualTo(payments);
    }
}
