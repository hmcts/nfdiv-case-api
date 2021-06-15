package uk.gov.hmcts.divorce.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.common.model.Gender;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RELATIONSHIP_COURT_HEADER;

@Component
public class CommonContent {

    private static final String APPLY_FOR_DIVORCE = "Divorce service";
    private static final String END_CIVIL_PARTNERSHIP = "End a civil partnership service";

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public Map<String, String> templateVarsFor(final CaseData caseData) {

        final HashMap<String, String> templateVars = new HashMap<>();

        templateVars.put(FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateVars.put(LAST_NAME, caseData.getApplicant1().getLastName());

        Map<String, String> configTemplateVars = emailTemplatesConfig.getTemplateVars();

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(RELATIONSHIP, DIVORCE_APPLICATION);
            templateVars.put(RELATIONSHIP_COURT_HEADER, APPLY_FOR_DIVORCE);
            templateVars.put(COURT_EMAIL, configTemplateVars.get(DIVORCE_COURT_EMAIL));
        } else {
            templateVars.put(RELATIONSHIP, APPLICATION_TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(RELATIONSHIP_COURT_HEADER, END_CIVIL_PARTNERSHIP);
            templateVars.put(COURT_EMAIL, configTemplateVars.get(DISSOLUTION_COURT_EMAIL));
        }

        return templateVars;
    }

    public String getService(DivorceOrDissolution divorceOrDissolution) {
        return divorceOrDissolution.isDivorce() ? "divorce" : "civil partnership";
    }

    public String applicant1GetPartner(CaseData caseData) {
        return caseData.getDivorceOrDissolution().isDivorce()
            ? caseData.getApplicant2().getGender() == Gender.MALE ? "husband" : "wife" : "civil partner";
    }

    public String applicant2GetPartner(CaseData caseData) {
        return caseData.getDivorceOrDissolution().isDivorce()
            ? caseData.getApplicant1().getGender() == Gender.MALE ? "husband" : "wife" : "civil partner";
    }
}
