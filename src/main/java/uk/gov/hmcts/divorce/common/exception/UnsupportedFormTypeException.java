package uk.gov.hmcts.divorce.common.exception;

import static java.lang.String.format;

public class UnsupportedFormTypeException extends RuntimeException {

    private static final long serialVersionUID = 3563983848385365880L;

    public UnsupportedFormTypeException(String formType) {
        super(format("\"%s\" form type is not supported", formType));
    }

}
