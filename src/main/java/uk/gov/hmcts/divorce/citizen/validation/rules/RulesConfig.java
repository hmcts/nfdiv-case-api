package uk.gov.hmcts.divorce.citizen.validation.rules;

import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RulesConfig {

    public static final String SUBMITTED_CASE_DATA_RULE_BOOK = "uk.gov.hmcts.divorce.citizen.validation.rules.caseData";

    @Bean("SubmittedCaseDataRuleBook")
    public RuleBookRunner submittedCaseDataRuleBook() {
        return new RuleBookRunner(SUBMITTED_CASE_DATA_RULE_BOOK);
    }


}
