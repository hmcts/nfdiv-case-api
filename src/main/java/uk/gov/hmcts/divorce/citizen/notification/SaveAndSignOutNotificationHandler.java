package uk.gov.hmcts.divorce.citizen.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;

@Component
public class SaveAndSignOutNotificationHandler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void notifyApplicant(CaseData caseData) {
        Map<String, String> templateVars = new HashMap<>();
        Map<String, String> configTemplateVars = emailTemplatesConfig.getTemplateVars();
        String signInUrlKey = caseData.getDivorceOrDissolution().isDivorce() ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL;
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY, configTemplateVars.get(signInUrlKey));

        commonContent.apply(templateVars, caseData);

        notificationService.sendEmail(
            caseData.getPetitionerEmail(),
            SAVE_SIGN_OUT,
            templateVars,
            caseData.getLanguagePreference()
        );
    }
}
