package uk.gov.hmcts.divorce.payment.rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentCallbackRuleEngine {

    private final List<PaymentCallbackRule> rules;

    public Optional<PaymentCallbackRule> find(State state, String serviceRequestRef, CaseData data) {
        return rules.stream()
            .filter(rule -> rule.matches(state, serviceRequestRef, data))
            .findFirst();
    }
}
