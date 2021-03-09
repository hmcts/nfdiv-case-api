package uk.gov.hmcts.reform.divorce.caseapi.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = NotificationException.class)
    public ResponseEntity<Object> handleNotificationException(NotificationException notificationException) {
        log.error(notificationException.getMessage(), notificationException);
        NotificationClientException notificationClientException =
            (NotificationClientException) notificationException.getCause();
        return new ResponseEntity<>(
            notificationClientException.getMessage(),
            new HttpHeaders(),
            notificationClientException.getHttpResult()
        );
    }
}
