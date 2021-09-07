package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.dateTimeFormatter;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ENDING_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_DIVORCE;

@Component
@Slf4j
public class ApplicationIssuedNotification {

    private static final String SERVICE = "service";
    private static final String DIVORCE_SERVICE = "divorce service";
    private static final String CIVIL_PARTNERSHIP_SERVICE = "End A Civil Partnership Service";
    private static final String GOV_UK_LINK = "gov uk link";
    private static final String DIVORCE_GOV_UK_LINK = "https://www.gov.uk/divorce";
    private static final String CIVIL_PARTNERSHIP_GOV_UK_LINK = "https://www.gov.uk/end-civil-partnership";
    private static final String CITIZENS_ADVICE_LINK = "citizens advice link";
    private static final String DIVORCE_CITIZENS_ADVICE_LINK = "https://www.citizensadvice.org.uk/family/how-to-separate1/getting-a-divorce/";
    private static final String CIVIL_PARTNERSHIP_CITIZENS_ADVICE_LINK = "https://www.citizensadvice.org.uk/family/how-to-separate1/ending-a-civil-partnership/";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToSoleApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        log.info("Sending sole application issued notification to applicant 1 for case : {}", id);

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(SERVICE, DIVORCE_SERVICE);
            templateVars.put(GOV_UK_LINK, DIVORCE_GOV_UK_LINK);
            templateVars.put(CITIZENS_ADVICE_LINK, DIVORCE_CITIZENS_ADVICE_LINK);
        } else {
            templateVars.put(SERVICE, CIVIL_PARTNERSHIP_SERVICE);
            templateVars.put(GOV_UK_LINK, CIVIL_PARTNERSHIP_GOV_UK_LINK);
            templateVars.put(CITIZENS_ADVICE_LINK, CIVIL_PARTNERSHIP_CITIZENS_ADVICE_LINK);
        }

        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getApplication().getIssueDate().plusDays(14).format(dateTimeFormatter));

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOL_APPLICANT_APPLICATION_ACCEPTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToJointApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        log.info("Sending joint application issued notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICATION_ACCEPTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToJointApplicant2(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        log.info("Sending joint application issued notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICATION_ACCEPTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    private Map<String, String> setTemplateVariables(CaseData caseData, Long id, Applicant applicant, Applicant respondent) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, applicant, respondent);

        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(dateTimeFormatter));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION);

            templateVars.put(PROCESS, DIVORCE_PROCESS);
            templateVars.put(ACCOUNT, DIVORCE_ACCOUNT);
            templateVars.put(APPLICATION_TYPE, YOUR_DIVORCE);

        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP);

            templateVars.put(PROCESS, CIVIL_PARTNERSHIP_PROCESS);
            templateVars.put(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT);
            templateVars.put(APPLICATION_TYPE, ENDING_CIVIL_PARTNERSHIP);

        }

        return templateVars;
    }
}
