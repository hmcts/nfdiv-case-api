package uk.gov.hmcts.divorce.caseworker.service.notification;

import com.google.common.collect.ImmutableList;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.RetentionPeriodDuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@ExtendWith(MockitoExtension.class)
class StateReportNotificationTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StateReportNotification stateReportNotification;

    private byte[] fileContents;
    private String fileName;
    private RetentionPeriodDuration retentionPeriodDuration;
    private HashMap<String, Object> templateVars;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        stateReportNotification.emailTo = "test@example.com";
        fileContents = "test file contents".getBytes();
        fileName = "testFile.csv";
        retentionPeriodDuration = mock(RetentionPeriodDuration.class); // Assuming this is not the focus of the test
        templateVars = new HashMap<>();
    }

    @Test
    void shouldReturnTrueAndAddLinkToFileWhenUploadIsSuccessful() {
        JSONObject mockJsonResponse = new JSONObject();
        mockJsonResponse.put("file_url", "http://uploaded-file-link.com");

        try (MockedStatic<NotificationClient> mockedStatic = mockStatic(NotificationClient.class)) {
            mockedStatic.when(() -> prepareUpload(any(byte[].class), anyString(), eq(false), eq(retentionPeriodDuration)))
                .thenReturn(mockJsonResponse);

            boolean result = stateReportNotification.prepareNotificationUpload(
                fileContents, fileName, retentionPeriodDuration, templateVars);

            assertTrue(result, "The method should return true when upload is successful");
            assertTrue(templateVars.containsKey("link_to_file"), "The templateVars should contain 'link_to_file'");
            assertEquals(templateVars.get("link_to_file"), mockJsonResponse, "The JSONObject should be correctly set in templateVars");
        }
    }

    @Test
    void shouldReturnFalseWhenNotificationClientExceptionOccurs() {
        try (MockedStatic<NotificationClient> mockedStatic = mockStatic(NotificationClient.class)) {
            mockedStatic.when(() -> prepareUpload(any(byte[].class), anyString(), eq(false), eq(retentionPeriodDuration)))
                .thenThrow(new NotificationClientException("Upload failed"));

            boolean result = stateReportNotification.prepareNotificationUpload(
                fileContents, fileName, retentionPeriodDuration, templateVars);

            assertFalse(result, "The method should return false when upload fails due to NotificationClientException");
            assertFalse(templateVars.containsKey("link_to_file"), "The templateVars should not contain 'link_to_file' when upload fails");
        }
    }

    @Test
    void shouldNotSendEmailWhenEmailToIsNull() throws NotificationClientException, IOException {
        stateReportNotification.emailTo = null;  // Simulate no email address

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
}
