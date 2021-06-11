package uk.gov.hmcts.divorce.common.config.advice;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.divorce.common.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.divorce.notification.exception.NotificationException;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
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

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UnAuthorisedServiceException.class)
    public ResponseEntity<Object> handleUnAuthorisedServiceException(
        UnAuthorisedServiceException unAuthorisedServiceException
    ) {
        return new ResponseEntity<>(
            unAuthorisedServiceException.getMessage(),
            HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Object> handleFeignException(FeignException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.status(exception.status()).body(
            String.format("%s - %s", exception.getMessage(), exception.contentUTF8())
        );
   
    @ExceptionHandler(DocumentDownloadException.class)
    public ResponseEntity<Object> handleDocumentDownloadException() {
        return new ResponseEntity<>(
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
