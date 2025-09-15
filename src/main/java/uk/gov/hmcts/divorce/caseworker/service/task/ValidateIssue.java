package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.exception.InvalidDataException;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateIssue;

@Component
@Slf4j
public class ValidateIssue implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> details) {
        log.info("Validating Issue for Case Id: {}", details.getId());

        final List<String> caseValidationErrors = validateIssue(details.getData());

        if (CollectionUtils.isNotEmpty(caseValidationErrors)) {
            throw new InvalidDataException(
                "Case data is not valid for application issue",
                null,
                caseValidationErrors
            );
        }

        return details;
    }
}
