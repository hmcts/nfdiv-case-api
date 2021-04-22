package uk.gov.hmcts.divorce.citizen.validation.rules;

import com.deliveredtechnologies.rulebook.model.RuleBook;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class RulesConfig {

    public static final String RULEBOOK = "uk.gov.hmcts.divorce.citizen.validation.rules.caseData";

    @SuppressWarnings("unchecked")
    @Bean("RuleBook")
    public RuleBook<List<String>> ruleBook() {
        return new RuleBookRunner(RULEBOOK);
    }


}
