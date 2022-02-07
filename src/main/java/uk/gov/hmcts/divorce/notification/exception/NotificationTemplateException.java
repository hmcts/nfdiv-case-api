package uk.gov.hmcts.divorce.notification.exception;

public class NotificationTemplateException extends RuntimeException {

    private static final long serialVersionUID = 3160179487262446549L;

    public NotificationTemplateException(final String message) {
        super(message);
    }
}
