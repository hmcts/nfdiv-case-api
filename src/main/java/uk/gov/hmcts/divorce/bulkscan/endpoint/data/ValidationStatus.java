package uk.gov.hmcts.divorce.bulkscan.endpoint.data;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public enum ValidationStatus {
    SUCCESS,
    WARNINGS,
    ERRORS;

    public static ValidationStatus getValidationStatus(List<String> errors, List<String> warnings) {
        if (!isEmpty(errors)) {
            return ERRORS;
        }
        if (!isEmpty(warnings)) {
            return WARNINGS;
        }
        return ValidationStatus.SUCCESS;
    }
}
