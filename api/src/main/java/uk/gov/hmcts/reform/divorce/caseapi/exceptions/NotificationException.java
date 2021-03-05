package uk.gov.hmcts.reform.divorce.caseapi.exceptions;

public class NotificationException extends RuntimeException {
    private static final long serialVersionUID = 5604833464289587151L;

    public NotificationException(Exception cause) {
        super(cause);
    }
}
