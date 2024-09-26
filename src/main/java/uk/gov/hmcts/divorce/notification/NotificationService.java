package uk.gov.hmcts.divorce.notification;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    @Value("${uk.gov.notify.email.replyToId}")
    private String replyToId;

    public void sendEmail(
        String destinationAddress,
        EmailTemplateName template,
        Map<? extends String, ? extends Object> templateVars,
        LanguagePreference languagePreference,
        Long caseId
    ) {
        String referenceId = String.format("%s-%s", caseId, UUID.randomUUID());
        Map<String,Object> templateVarsObj = (templateVars != null) ? new HashMap<>(templateVars) : null;

        try {
            String templateId = emailTemplatesConfig.getTemplates().get(languagePreference).get(template.name());

            log.info("Sending email for reference id : {} using template : {}", referenceId, templateId);

            SendEmailResponse sendEmailResponse =
                notificationClient.sendEmail(
                    templateId,
                    destinationAddress,
                    templateVarsObj,
                    referenceId,
                    replyToId
                );

            log.info("Successfully sent email with notification id {} and reference {}",
                sendEmailResponse.getNotificationId(),
                sendEmailResponse.getReference().orElse(referenceId)
            );

        } catch (NotificationClientException notificationClientException) {
            log.error("Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                notificationClientException.getMessage(),
                notificationClientException
            );
            final String message = notificationClientException.getMessage()
                + format(" Exception for Case ID: %s", caseId);
            throw new NotificationException(message, notificationClientException);
        }
    }
}
