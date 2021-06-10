package uk.gov.hmcts.divorce.print.exception;

public class DocumentDownloadException extends RuntimeException {

    private static final long serialVersionUID = 7442994120484411077L;

    public DocumentDownloadException(String message) {
        super(message);
    }

    public DocumentDownloadException(String message, Exception cause) {
        super(message, cause);
    }
}
