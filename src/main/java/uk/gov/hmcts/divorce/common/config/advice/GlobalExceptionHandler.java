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
import uk.gov.hmcts.divorce.document.print.exception.InvalidResourceException;
import uk.gov.hmcts.divorce.notification.exception.NotificationException;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.out.BspErrorResponse;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.status;

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
        return status(HttpStatus.UNAUTHORIZED).build();
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

        return status(exception.status()).body(
            String.format("%s - %s", exception.getMessage(), exception.contentUTF8())
        );
    }

    @ExceptionHandler(InvalidResourceException.class)
    public ResponseEntity<Object> handleInvalidResourceException() {
        return new ResponseEntity<>(
            INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<Object> handleInvalidDataException(InvalidDataException exception) {
        log.warn(exception.getMessage(), exception);

        return status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                BspErrorResponse.builder()
                    .errors(exception.getErrors())
                    .warnings(exception.getWarnings())
                    .build()
            );
    }

    @ExceptionHandler(UnsupportedFormTypeException.class)
    public ResponseEntity<Object> handleUnsupportedFormTypeException(UnsupportedFormTypeException exception) {
        log.warn(exception.getMessage(), exception);

        return status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                BspErrorResponse.builder()
                    .errors(Collections.singletonList(exception.getMessage()))
                    .build()
            );
    }
}
