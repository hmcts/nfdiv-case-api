package uk.gov.hmcts.divorce.payment.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

class ApplicationPaymentMadeRuleTest {

    private ApplicationPaymentMadeRule rule;

    @BeforeEach
    void setup() {
        rule = new ApplicationPaymentMadeRule();
    }

    @Test
    void returnsFalseIfStateIsNotAwaitingPayment() {
        final CaseData data = CaseData.builder()
            .application(Application.builder()
                .applicationFeeServiceRequestReference(TEST_SERVICE_REFERENCE)
                .build())
            .build();

        final boolean result = rule.matches(State.Holding, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isFalse();
    }

    @Test
    void returnsFalseIfServiceRequestDoesNotMatch() {
        final CaseData data = CaseData.builder()
            .application(Application.builder()
                .applicationFeeServiceRequestReference(TEST_SERVICE_REFERENCE)
                .build())
            .build();

        final boolean result = rule.matches(State.AwaitingPayment, "dummy", data);

        assertThat(result).isFalse();
    }

    @Test
    void returnsTrueIfStateIsAwaitingPaymentAndServiceRequestMatches() {
        final CaseData data = CaseData.builder()
            .application(Application.builder()
                .applicationFeeServiceRequestReference(TEST_SERVICE_REFERENCE)
                .build())
            .build();

        final boolean result = rule.matches(State.AwaitingPayment, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isTrue();
    }

    @Test
    void returnsPaymentMadeEvent() {
        assertThat(rule.paymentMadeEvent()).isEqualTo(CITIZEN_PAYMENT_MADE);
    }

    @Test
    void returnsPayments() {
        final List<ListValue<Payment>> payments = List.of(
            ListValue.<Payment>builder().value(Payment.builder().build()).build()
        );

        final CaseData data = CaseData.builder()
            .application(Application.builder()
                .applicationPayments(payments)
                .build())
            .build();

        assertThat(rule.getPayments(data)).isEqualTo(payments);
    }
}
