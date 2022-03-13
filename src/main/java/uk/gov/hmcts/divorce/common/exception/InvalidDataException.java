package uk.gov.hmcts.divorce.common.exception;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InvalidDataException extends RuntimeException {

    private final List<String> errors;
    private final List<String> warnings;

    public InvalidDataException(String message, List<String> warnings, List<String> errors) {
        super(message);
        this.warnings = Optional.ofNullable(warnings).orElse(Collections.emptyList());
        this.errors = Optional.ofNullable(errors).orElse(Collections.emptyList());
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
