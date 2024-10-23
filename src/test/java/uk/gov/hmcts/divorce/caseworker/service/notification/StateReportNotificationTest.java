package uk.gov.hmcts.divorce.caseworker.service.notification;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.RetentionPeriodDuration;

import java.io.IOException;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class StateReportNotificationTest {

    @Mock
    private NotificationService notificationService;

    private StateReportNotification stateReportNotification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        stateReportNotification = new StateReportNotification();
        stateReportNotification.notificationService = notificationService;  // Inject the mock dependency manually
        stateReportNotification.emailTo = "test@example.com";  // Set the email you want for testing
    }

    @Test
    void shouldSendEmailWhenPrepareNotificationUploadSucceeds() throws NotificationClientException, IOException {
        ImmutableList.Builder<String> preparedData = ImmutableList.builder();
        preparedData.add("header, data\n");
        String reportName = "testReport";

        StateReportNotification spyNotification = spy(stateReportNotification);

        doReturn(true).when(spyNotification).prepareNotificationUpload(any(byte[].class), anyString(),
            any(RetentionPeriodDuration.class),  ArgumentMatchers.<HashMap<String, Object>>any());

        spyNotification.send(preparedData, reportName);

        verify(notificationService).sendEmailWithString(
            anyString(),
            eq(EmailTemplateName.AUTOMATED_DAILY_REPORT),
            ArgumentMatchers.<HashMap<String, Object>>any(),
            eq(uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH),
            eq(reportName)
        );
    }

    @Test
    void shouldLogErrorWhenPrepareNotificationUploadFails() throws NotificationClientException, IOException {
        ImmutableList.Builder<String> preparedData = ImmutableList.builder();
        preparedData.add("header, data\n");
        String reportName = "testReport";

        StateReportNotification spyNotification = spy(stateReportNotification);

        doReturn(false).when(spyNotification).prepareNotificationUpload(any(byte[].class), anyString(),
            any(RetentionPeriodDuration.class),  ArgumentMatchers.<HashMap<String, Object>>any());

        spyNotification.send(preparedData, reportName);

        verify(notificationService, never()).sendEmailWithString(anyString(), any(), any(), any(), any());
    }
}

