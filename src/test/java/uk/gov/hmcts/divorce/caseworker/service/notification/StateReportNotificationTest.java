package uk.gov.hmcts.divorce.caseworker.service.notification;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.RetentionPeriodDuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StateReportNotificationTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @Spy
    @InjectMocks
    private NotificationService notificationService;

    private StateReportNotification stateReportNotification;

    private byte[] fileContents;
    private String fileName;
    private RetentionPeriodDuration retentionPeriodDuration;
    private HashMap<String, Object> templateVars;

    @BeforeEach
    void setUp() {
        stateReportNotification = new StateReportNotification(notificationService);
        stateReportNotification.recipientEmailAddressesCsv = "test@example.com";
        fileContents = "test file contents".getBytes();
        fileName = "testFile.csv";
        retentionPeriodDuration = mock(RetentionPeriodDuration.class); // Assuming this is not the focus of the test
        templateVars = new HashMap<>();
    }

    @Test
    void shouldNotSendEmailWhenEmailToIsNull() throws NotificationClientException, IOException {
        stateReportNotification.recipientEmailAddressesCsv = null;  // Simulate no email address

        stateReportNotification.send(ImmutableList.builder(), "testReport");

        verify(notificationService, never()).sendEmailWithString(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void shouldNotSendEmailWhenPrepareNotificationUploadFails() throws NotificationClientException, IOException {
        StateReportNotification spyNotification = spy(stateReportNotification);

        doReturn(false).when(spyNotification).prepareNotificationUpload(
            any(byte[].class), anyString(), any(RetentionPeriodDuration.class), ArgumentMatchers.<HashMap<String, Object>>any());

        spyNotification.send(ImmutableList.builder(), "testReport");

        verify(notificationService, never()).sendEmailWithString(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void shouldSendEmailWhenPrepareNotificationUploadSucceeds() throws NotificationClientException, IOException {
        StateReportNotification spyNotification = spy(stateReportNotification);

        doReturn(true).when(spyNotification).prepareNotificationUpload(
            any(byte[].class), anyString(), any(RetentionPeriodDuration.class), ArgumentMatchers.<HashMap<String, Object>>any());

        spyNotification.send(ImmutableList.builder(), "testReport");

        Map<String, Object> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put("reportName", "testReport");

        verify(notificationService).sendEmailWithString(
            "test@example.com",
            EmailTemplateName.AUTOMATED_DAILY_REPORT,
            expectedTemplateVars,
            uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH,
            "testReport"
        );
    }

    @Test
    void shouldSendMultipleEmailsWhenReportEmailAddressesIsAList() throws NotificationClientException, IOException {
        stateReportNotification.recipientEmailAddressesCsv = "test@example.com,test2@example.com";

        StateReportNotification spyNotification = spy(stateReportNotification);

        doReturn(true).when(spyNotification).prepareNotificationUpload(
            any(byte[].class), anyString(), any(RetentionPeriodDuration.class), ArgumentMatchers.<HashMap<String, Object>>any());

        spyNotification.send(ImmutableList.builder(), "testReport");

        Map<String, Object> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put("reportName", "testReport");

        verify(notificationService).sendEmailWithString(
            "test@example.com",
            EmailTemplateName.AUTOMATED_DAILY_REPORT,
            expectedTemplateVars,
            uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH,
            "testReport"
        );

        verify(notificationService).sendEmailWithString(
            "test2@example.com",
            EmailTemplateName.AUTOMATED_DAILY_REPORT,
            expectedTemplateVars,
            uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH,
            "testReport"
        );
    }
}
