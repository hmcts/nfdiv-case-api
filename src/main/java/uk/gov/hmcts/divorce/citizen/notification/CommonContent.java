package uk.gov.hmcts.divorce.citizen.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;

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

    private static final String APPLY_FOR_DIVORCE = "Apply for a divorce";
    private static final String END_CIVIL_PARTNERSHIP = "End a civil partnership";

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void apply(Map<String, String> templateVars, CaseData caseData) {
        templateVars.put(FIRST_NAME, caseData.getPetitionerFirstName());
        templateVars.put(LAST_NAME, caseData.getPetitionerLastName());

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
    }
}
