package uk.gov.hmcts.reform.divorce.caseapi.exceptions;

public class NotificationException extends RuntimeException {

    public NotificationException(Exception cause) {
        super(cause);
    }

}
