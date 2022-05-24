package uk.gov.hmcts.divorce.common.exception;

public class InvalidOperationException extends RuntimeException {

    private static final long serialVersionUID = 1888206621734610397L;

    public InvalidOperationException(final String message) {
        super(message);
    }
}
