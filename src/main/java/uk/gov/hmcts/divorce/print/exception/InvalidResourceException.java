package uk.gov.hmcts.divorce.print.exception;

public class InvalidResourceException extends RuntimeException {

    private static final long serialVersionUID = 7442994120484411077L;

    public InvalidResourceException(String message) {
        super(message);
    }

    public InvalidResourceException(String message, Exception cause) {
        super(message, cause);
    }
}
