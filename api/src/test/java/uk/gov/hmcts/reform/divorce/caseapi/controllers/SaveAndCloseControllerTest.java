package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.hmcts.reform.divorce.caseapi.notification.handler.SaveAndSignOutNotificationHandler;
import uk.gov.service.notify.NotificationClientException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SaveAndCloseControllerTest {

    @Autowired
    private SaveAndCloseController saveAndCloseController;

    @MockBean
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() {
        saveAndCloseController.saveAndClose(callbackRequest());

        verify(saveAndSignOutNotificationHandler).notifyApplicant(caseData());
        verifyNoMoreInteractions(saveAndSignOutNotificationHandler);
    }

    @Test
    public void givenHandlerThrowsExceptionWhenCallbackIsInvokedThenReturnBadRequest() {
        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(saveAndSignOutNotificationHandler).notifyApplicant(eq(caseData()));

        Throwable thrown = assertThrows(NotificationException.class,
            () -> saveAndCloseController.saveAndClose(callbackRequest()));

        assertThat(thrown.getCause().getMessage(), is("All template params not passed"));
    }
}
