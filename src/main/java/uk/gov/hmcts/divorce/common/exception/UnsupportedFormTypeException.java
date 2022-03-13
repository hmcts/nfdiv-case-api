package uk.gov.hmcts.divorce.common.exception;

import static java.lang.String.format;

public class UnsupportedFormTypeException extends RuntimeException {

    public UnsupportedFormTypeException(String formType) {
        super(format("\"%s\" form type is not supported", formType));
    }

}
