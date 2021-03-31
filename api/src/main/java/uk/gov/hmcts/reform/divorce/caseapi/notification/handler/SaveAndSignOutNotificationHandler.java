package uk.gov.hmcts.reform.divorce.caseapi.notification.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.caseapi.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.caseapi.service.NotificationService;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.caseapi.enums.EmailTemplateNames.SAVE_SIGN_OUT;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.APPLY_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.COURT_EMAIL;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.RELATIONSHIP_COURT_HEADER;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;

@Component
public class SaveAndSignOutNotificationHandler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void notifyApplicant(CaseData caseData) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(FIRST_NAME, caseData.getPetitionerFirstName());
        templateVars.put(LAST_NAME, caseData.getPetitionerLastName());

        Map<String, Map<String, String>> configTemplateVars = emailTemplatesConfig.getTemplateVars();

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(RELATIONSHIP, DIVORCE_APPLICATION);
            templateVars.put(RELATIONSHIP_COURT_HEADER, APPLY_FOR_DIVORCE);

            String courtEmail = configTemplateVars.get(SAVE_SIGN_OUT.name()).get("divCourtEmail");
            templateVars.put(COURT_EMAIL, courtEmail);

            String signInUrl = configTemplateVars.get(SAVE_SIGN_OUT.name()).get(SIGN_IN_DIVORCE_URL);
            templateVars.put(SIGN_IN_URL_NOTIFY_KEY, signInUrl);
        } else {
            templateVars.put(RELATIONSHIP, APPLICATION_TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(RELATIONSHIP_COURT_HEADER, END_CIVIL_PARTNERSHIP);

            String courtEmail = configTemplateVars.get(SAVE_SIGN_OUT.name()).get("civilPartnershipCourtEmail");
            templateVars.put(COURT_EMAIL, courtEmail);

            String signInUrl = configTemplateVars.get(SAVE_SIGN_OUT.name()).get(SIGN_IN_DISSOLUTION_URL);
            templateVars.put(SIGN_IN_URL_NOTIFY_KEY, signInUrl);
        }

        notificationService.sendEmail(
            caseData.getPetitionerEmail(),
            emailTemplatesConfig.getTemplates().get(ENGLISH).get(SAVE_SIGN_OUT.name()),
            templateVars,
            ENGLISH // to be updated later based on language preference
        );
    }
}
