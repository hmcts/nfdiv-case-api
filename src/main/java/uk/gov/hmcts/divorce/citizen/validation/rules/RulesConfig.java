package uk.gov.hmcts.divorce.citizen.validation.rules;

import com.deliveredtechnologies.rulebook.model.RuleBook;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class RulesConfig {

    public static final String SUBMITTED_CASE_DATA_RULE_BOOK = "uk.gov.hmcts.divorce.citizen.validation.rules.caseData";

    @SuppressWarnings("unchecked")
    @Bean("SubmittedCaseDataRuleBook")
    public RuleBook<List<String>> submittedCaseDataRuleBook() {
        return new RuleBookRunner(SUBMITTED_CASE_DATA_RULE_BOOK);
    }


}
