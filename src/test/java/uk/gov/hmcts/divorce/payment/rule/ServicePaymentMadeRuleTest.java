package uk.gov.hmcts.divorce.payment.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenServicePaymentMade.CITIZEN_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

class ServicePaymentMadeRuleTest {

    private ServicePaymentMadeRule rule;

    @BeforeEach
    void setup() {
        rule = new ServicePaymentMadeRule();
    }

    @Test
    void returnsFalseIfStateIsNotAwaitingServicePayment() {
        final CaseData data = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder().serviceRequestReference(TEST_SERVICE_REFERENCE).build())
                .build())
            .build();

        final boolean result = rule.matches(State.Holding, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isFalse();
    }

    @Test
    void returnsFalseIfServiceRequestDoesNotMatch() {
        final CaseData data = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder().serviceRequestReference(TEST_SERVICE_REFERENCE).build())
                .build())
            .build();

        final boolean result = rule.matches(State.AwaitingServicePayment, "dummy", data);

        assertThat(result).isFalse();
    }

    @Test
    void returnsTrueIfStateIsAwaitingServicePaymentAndServiceRequestMatches() {
        final CaseData data = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder().serviceRequestReference(TEST_SERVICE_REFERENCE).build())
                .build())
            .build();

        final boolean result = rule.matches(State.AwaitingServicePayment, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isTrue();
    }

    @Test
    void returnsServicePaymentMadeEvent() {
        assertThat(rule.paymentMadeEvent()).isEqualTo(CITIZEN_SERVICE_PAYMENT);
    }

    @Test
    void returnsPayments() {
        final List<ListValue<Payment>> payments = List.of(
            ListValue.<Payment>builder().value(Payment.builder().build()).build()
        );

        final CaseData data = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePayments(payments)
                .build())
            .build();

        assertThat(rule.getPayments(data)).isEqualTo(payments);
    }
}
