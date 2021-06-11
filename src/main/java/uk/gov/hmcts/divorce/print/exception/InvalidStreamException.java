package uk.gov.hmcts.divorce.print.exception;

public class InvalidStreamException extends RuntimeException {

    private static final long serialVersionUID = 7442994120484411077L;

    public InvalidStreamException(String message) {
        super(message);
    }

    public InvalidStreamException(String message, Exception cause) {
        super(message, cause);
    }
}
