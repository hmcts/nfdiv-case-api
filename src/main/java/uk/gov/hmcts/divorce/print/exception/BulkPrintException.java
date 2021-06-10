package uk.gov.hmcts.divorce.print.exception;

public class BulkPrintException extends RuntimeException {

    private static final long serialVersionUID = 7442994120484411077L;

    public BulkPrintException(String message, Exception cause) {
        super(message, cause);
    }
}
