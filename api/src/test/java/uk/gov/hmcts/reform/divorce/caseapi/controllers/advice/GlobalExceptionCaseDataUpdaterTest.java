package uk.gov.hmcts.reform.divorce.caseapi.controllers.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionCaseDataUpdaterTest {

    @Test
    public void shouldHandleNotificationException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final NotificationException notificationException = mock(NotificationException.class);

        when(notificationException.getCause()).thenReturn(new NotificationClientException("some exception"));

        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleNotificationException(notificationException);

        assertThat(actualResponse.getStatusCode(), is(HttpStatus.BAD_REQUEST));

        assertThat(actualResponse.getBody(), is("some exception"));
    }
}
