package uk.gov.hmcts.divorce.bulkscan.exception;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InvalidOcrDataException extends RuntimeException {
    private static final long serialVersionUID = -2240235036273410173L;

    private final List<String> errors;
    private final List<String> warnings;

    public InvalidOcrDataException(String message, List<String> warnings, List<String> errors) {
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
