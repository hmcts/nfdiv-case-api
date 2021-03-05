package uk.gov.hmcts.reform.divorce.caseapi.notification.handler;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.caseapi.enums.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference;
import uk.gov.hmcts.reform.divorce.caseapi.service.NotificationService;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.LAST_NAME;

public class SaveAndSignOutNotificationHandler {

    @Autowired
    private NotificationService notificationService;

    public void notifyApplicant(CaseData  caseData) {
        @SuppressWarnings("PMD") // UseConcurrentHashMap
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(FIRST_NAME, caseData.getD8PetitionerFirstName());
        templateVars.put(LAST_NAME, caseData.getD8PetitionerLastName());

        notificationService.sendEmail(
            caseData.getD8PetitionerEmail(),
            EmailTemplateNames.SAVE_SIGN_OUT,
            templateVars,
            LanguagePreference.ENGLISH
        );
    }
}
