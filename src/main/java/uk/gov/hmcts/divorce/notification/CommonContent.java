package uk.gov.hmcts.divorce.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NO;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YES;

@Component
public class CommonContent {

    @Autowired
    private EmailTemplatesConfig config;

    public Map<String, String> commonTemplateVars(CaseData caseData, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(IS_DIVORCE, isDivorce(caseData) ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !isDivorce(caseData) ? YES : NO);
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(PARTNER, getPartner(caseData, partner));
        templateVars.put(COURT_EMAIL,
            config.getTemplateVars().get(isDivorce(caseData) ? DIVORCE_COURT_EMAIL : DISSOLUTION_COURT_EMAIL));
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY,
            config.getTemplateVars().get(isDivorce(caseData) ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));
        return templateVars;
    }

    public static boolean isDivorce(CaseData caseData) {
        return caseData.getDivorceOrDissolution().isDivorce();
    }

    public String getService(DivorceOrDissolution divorceOrDissolution) {
        return divorceOrDissolution.isDivorce() ? "divorce" : "civil partnership";
    }

    public String getPartner(CaseData caseData, Applicant partner) {
        return isDivorce(caseData) ? partner.getGender() == Gender.MALE ? "husband" : "wife" : "civil partner";
    }

    public Map<String, String> commonNotificationTemplateVars(final CaseData caseData, final Long caseId) {

        final Map<String, String> templateVars = new HashMap<>();
        final Applicant applicant = caseData.getApplicant1();
        final Applicant respondent = caseData.getApplicant2();

        templateVars.put(APPLICANT_NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        templateVars.put(RESPONDENT_NAME, join(" ", respondent.getFirstName(), respondent.getLastName()));
        templateVars.put(APPLICATION_REFERENCE, formatId(caseId));

        return templateVars;
    }
}
