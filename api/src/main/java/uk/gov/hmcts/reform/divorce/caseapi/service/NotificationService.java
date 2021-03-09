package uk.gov.hmcts.reform.divorce.caseapi.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationClient notificationClient;

    public void sendEmail(
        String destinationAddress,
        String templateId,
        Map<String, String> templateVars,
        LanguagePreference languagePreference
    ) {
        String referenceId = UUID.randomUUID().toString();

        try {
            log.info("Sending email for reference id : {} using template : {}", referenceId, templateId);

            SendEmailResponse sendEmailResponse =
                notificationClient.sendEmail(
                    templateId,
                    destinationAddress,
                    templateVars,
                    referenceId
                );

            log.info("Successfully sent email with notification id {} and reference {}",
                sendEmailResponse.getNotificationId(),
                sendEmailResponse.getReference().get()
            );

        } catch (NotificationClientException notificationClientException) {
            log.error("Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                notificationClientException.getMessage(),
                notificationClientException
            );
            throw new NotificationException(notificationClientException);
        }
    }
}
