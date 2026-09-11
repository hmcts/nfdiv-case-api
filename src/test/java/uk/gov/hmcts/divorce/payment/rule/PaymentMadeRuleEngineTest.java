package uk.gov.hmcts.divorce.payment.rule;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

class PaymentMadeRuleEngineTest {

    private PaymentMadeRuleEngine engine;

    @Test
    void returnsFirstMatchingRule() {
        CaseData data = CaseData.builder().build();

        PaymentMadeRule rule1 = mock(PaymentMadeRule.class);
        PaymentMadeRule rule2 = mock(PaymentMadeRule.class);
        when(rule1.matches(State.Holding, TEST_SERVICE_REFERENCE, data)).thenReturn(false);
        when(rule1.matches(State.Holding, TEST_SERVICE_REFERENCE, data)).thenReturn(true);

        engine = new PaymentMadeRuleEngine(List.of(rule1, rule2));

        Optional<PaymentMadeRule> result =
            engine.find(State.Holding, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isPresent();
        assertThat(result).contains(rule1);
    }

    @Test
    void returnsEmptyOptionalIfNoMatchingRule() {
        CaseData data = CaseData.builder().build();

        engine = new PaymentMadeRuleEngine(Collections.emptyList());

        Optional<PaymentMadeRule> result =
            engine.find(State.Holding, TEST_SERVICE_REFERENCE, data);

        assertThat(result).isEmpty();
    }
}
