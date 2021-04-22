package uk.gov.hmcts.divorce.citizen.validation.service;


import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.Result;
import com.deliveredtechnologies.rulebook.model.RuleBook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ValidationResponse;
import uk.gov.hmcts.divorce.common.model.ValidationStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PetitionValidationService {

    @Autowired
    @Qualifier("RuleBook")
    private RuleBook<List<String>> ruleBook;

    public ValidationResponse validateCaseData(CaseData caseData) {
        log.info("Validating petitioner case data");
        NameValueReferableMap<CaseData> facts = new FactMap<>();

        facts.setValue("caseData", caseData);
        ruleBook.setDefaultResult(new ArrayList<>());
        ruleBook.run(facts);

        ValidationResponse validationResponse = ValidationResponse.builder()
            .validationStatus(ValidationStatus.SUCCESS.getValue())
            .build();

        ruleBook.getResult().map(Result::getValue)
            .ifPresent(result -> errorResponse(validationResponse, result));

        return validationResponse;

    }

    private void errorResponse(ValidationResponse validationResponse, List<String> result) {
        if (!result.isEmpty()) {
            validationResponse.setErrors(result);
            validationResponse.setValidationStatus(ValidationStatus.FAILED.getValue());
        }
    }

}
