package uk.gov.hmcts.divorce.common.exception;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InvalidDataException extends RuntimeException {

    private static final long serialVersionUID = 985127511558687731L;

    private final List<String> errors;

    public InvalidDataException(String message, List<String> warnings, List<String> errors) {
        super(message);
        this.errors = Optional.ofNullable(errors).orElse(Collections.emptyList());
    }

    public List<String> getErrors() {
        return errors;
    }

}
