package uk.gov.hmcts.divorce.payment.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenGeneralApplicationPaymentMade.CITIZEN_GENERAL_APPLICATION_PAYMENT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

class Applicant1GeneralApplicationPaymentMadeRuleTest {

    private Applicant1GeneralApplicationPaymentMadeRule rule;

    @BeforeEach
    void setup() {
        rule = new Applicant1GeneralApplicationPaymentMadeRule();
    }

    @Test
    void returnsTrueIfPaymentServiceRequestMatchesApplicant1GenAppServiceRequest() {
        final CaseData data = CaseData.builder()
            .applicant1(Applicant.builder().generalAppServiceRequest(TEST_SERVICE_REFERENCE).build())
            .build();

        final boolean result = rule.matches(State.Holding, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isTrue();
    }

    @Test
    void returnsFalseIfPaymentServiceRequestDoesNotMatchApplicant1GenAppServiceRequest() {
        final CaseData data = CaseData.builder()
            .applicant1(Applicant.builder().generalAppServiceRequest(TEST_SERVICE_REFERENCE).build())
            .build();

        final boolean result = rule.matches(State.Holding, "dummy", data);

        assertThat(result).isFalse();
    }

    @Test
    void returnsPaymentMadeEvent() {
        assertThat(rule.paymentMadeEvent()).isEqualTo(CITIZEN_GENERAL_APPLICATION_PAYMENT);
    }

    @Test
    void returnsPayments() {
        final List<ListValue<Payment>> payments = List.of(
            ListValue.<Payment>builder().value(Payment.builder().build()).build()
        );

        final CaseData data = CaseData.builder()
            .applicant1(Applicant.builder().generalAppPayments(payments).build())
            .build();

        assertThat(rule.getPayments(data)).isEqualTo(payments);
    }
}
