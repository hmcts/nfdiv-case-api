package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateIssue;

@Component
@Slf4j
public class ValidateIssue {
    public List<String> validate(final CaseDetails<CaseData, State> details) {
        log.info("Validating Issue for Case Id: {}", details.getId());
        final List<String> caseValidationErrors = validateIssue(details.getData());
        return caseValidationErrors;
    }
}
