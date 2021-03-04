package uk.gov.hmcts.reform.divorce.caseapi.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.caseapi.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    @Autowired
    private NotificationClient notificationClient;

    public void sendEmail(
        String destinationAddress,
        String templateName,
        Map<String, String> templateVars,
        LanguagePreference languagePreference
    ) {
        String referenceId = UUID.randomUUID().toString();
        String templateId = emailTemplatesConfig.getTemplates().get(languagePreference).get(templateName);
        Map<String, String> templateFields = emailTemplatesConfig.getTemplateVars().get(templateName);

        try {
            notificationClient.sendEmail(templateId, destinationAddress, templateFields, referenceId);
        } catch (NotificationClientException notificationClientException) {
            log.warn("Failed to send email. Reference ID: {}. Reason: {}", "emailToSend.getReferenceId()",
                notificationClientException.getMessage(), notificationClientException);
            throw new NotificationException(notificationClientException);
        }
    }
}
