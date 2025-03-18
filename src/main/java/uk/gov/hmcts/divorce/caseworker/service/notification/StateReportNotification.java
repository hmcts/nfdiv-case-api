package uk.gov.hmcts.divorce.caseworker.service.notification;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.RetentionPeriodDuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.AUTOMATED_DAILY_REPORT;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Component
@Slf4j
public class StateReportNotification {

    @Value("${report-email}")
    String recipientEmailAddressesCsv;

    @Autowired
    private NotificationService notificationService;

    private final RetentionPeriodDuration retentionPeriodDuration = new RetentionPeriodDuration(26, ChronoUnit.WEEKS);

    private byte[] convertToBytes(ImmutableList.Builder<String> preparedData) {
        String csvContent = String.join("", preparedData.build());
        return csvContent.getBytes(StandardCharsets.UTF_8);
    }

    public void send(ImmutableList.Builder<String> preparedData, String reportName) throws NotificationClientException, IOException {
        log.info("Sending Report Email Notification {}", reportName);

        EmailTemplateName templateName;
        templateName = AUTOMATED_DAILY_REPORT;
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("reportName", reportName);

        if (null == recipientEmailAddressesCsv) {
            log.error("Email address is not available for template id {} and daily report {} ", templateName, reportName);
            return;
        }

        if (!prepareNotificationUpload(convertToBytes(preparedData), reportName, retentionPeriodDuration,
            (HashMap<String, Object>) templateVars)) {
            log.error("Failed to prepare upload for daily report {} ", templateName, reportName);
        } else {
            String[] recipientEmailAddresses = recipientEmailAddressesCsv.split(",");

            Arrays.stream(recipientEmailAddresses)
                .map(String::trim)
                .forEach(emailAddress -> sendNotification(emailAddress, templateName, templateVars, reportName));
        }
    }

    private void sendNotification(
        String emailAddress, EmailTemplateName templateName,
        Map<String, Object> templateVars, String reportName
    ) {
        try {
            notificationService.sendEmailWithString(
                emailAddress,
                templateName,
                templateVars,
                ENGLISH,
                reportName
            );
            log.info("Successfully sent daily case state report notification {} to {}", reportName, emailAddress);
        } catch (Exception e) {
            log.info("Daily case state report notification failed for {} to {}", reportName, emailAddress);
        }
    }

    boolean prepareNotificationUpload(byte[] fileContents, String fileName, RetentionPeriodDuration retentionPeriodDuration,
                                             HashMap<String, Object> templateVars) {
        try {
            templateVars.put("link_to_file",
                prepareUpload(
                    fileContents, fileName,
                    false,
                    retentionPeriodDuration
                ));
        } catch (NotificationClientException e) {
            log.error("Error Preparing to send email to for daily report {} ", e.getMessage());
            return false;
        }
        return true;
    }
}
