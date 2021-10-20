package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.*;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.*;

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
    private static final String APPLICATION_TYPE_PROGRESS = "application type progress";
    private static final String THE_DIVORCE_APPLICATION = "the divorce application";
    private static final String FOR_A_DIVORCE = "for a divorce";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void sendToSoleApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        log.info("Sending sole application issued notification to applicant 1 for case : {}", id);

        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getApplication().getIssueDate().plusDays(14).format(DATE_TIME_FORMATTER));

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_APPLICATION_ACCEPTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToSoleRespondent(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        log.info("Sending sole application issued notification to respondent for case : {}", id);

        setInviteVariables(templateVars, caseData);
        templateVars.put(REMINDER_APPLICATION, APPLICATION);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            SOLE_RESPONDENT_APPLICATION_ACCEPTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendReminderToSoleRespondent(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        log.info("Sending reminder to respondent to register for case : {}", id);

        setInviteVariables(templateVars, caseData);
        templateVars.put(REMINDER_APPLICATION, REMINDER_APPLICATION_VALUE);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            SOLE_RESPONDENT_APPLICATION_ACCEPTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendPartnerNotRespondedToSoleApplicant(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        log.info("Sending the respondent has not responded notification to the applicant for case : {}", id);

        templateVars.put(REMINDER_APPLICATION, REMINDER_APPLICATION_VALUE);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED,
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

    public void notifyApplicantOfServiceToOverseasRespondent(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        log.info("Notifying sole applicant of application issue (case {}) to overseas respondent", id);

        boolean isDivorce = caseData.getDivorceOrDissolution().isDivorce();
        templateVars.put(APPLICATION_TYPE, isDivorce ? YOUR_DIVORCE : ENDING_YOUR_CIVIL_PARTNERSHIP);
        templateVars.put(PAPERS, isDivorce ? DIVORCE_PAPERS : DISSOLUTION_PAPERS);
        templateVars.put(TWENTY_EIGHT_DAY_DEADLINE, caseData.getApplication().getIssueDate().plusDays(28).format(DATE_TIME_FORMATTER));

        boolean hasEmail = !caseData.getApplicant2().getEmail().isEmpty();

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            hasEmail ? OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED : OVERSEAS_RESPONDENT_NO_EMAIL_APPLICATION_ISSUED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    private Map<String, String> setTemplateVariables(CaseData caseData, Long id, Applicant applicant, Applicant respondent) {
        boolean isDivorce = caseData.getDivorceOrDissolution().isDivorce();
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(caseData, applicant, respondent);
        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));

        if (caseData.getDivorceOrDissolution().isDivorce()) {

            templateVars.put(PROCESS, DIVORCE_PROCESS);
            templateVars.put(ACCOUNT, DIVORCE_ACCOUNT);
            templateVars.put(YOUR_UNION, YOUR_DIVORCE);
            templateVars.put(SERVICE, DIVORCE_SERVICE);
            templateVars.put(GOV_UK_LINK, DIVORCE_GOV_UK_LINK);
            templateVars.put(CITIZENS_ADVICE_LINK, DIVORCE_CITIZENS_ADVICE_LINK);

        } else {

            templateVars.put(PROCESS, CIVIL_PARTNERSHIP_PROCESS);
            templateVars.put(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT);
            templateVars.put(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP);
            templateVars.put(SERVICE, CIVIL_PARTNERSHIP_SERVICE);
            templateVars.put(GOV_UK_LINK, CIVIL_PARTNERSHIP_GOV_UK_LINK);
            templateVars.put(CITIZENS_ADVICE_LINK, CIVIL_PARTNERSHIP_CITIZENS_ADVICE_LINK);
        }

        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getApplication().getIssueDate().plusDays(16).format(DATE_TIME_FORMATTER));

        templateVars.put(APPLICATION, isDivorce ? DIVORCE_APPLICATION : APPLICATION_TO_END_CIVIL_PARTNERSHIP);
        templateVars.put(PROCESS, isDivorce ? DIVORCE_PROCESS : CIVIL_PARTNERSHIP_PROCESS);
        templateVars.put(ACCOUNT, isDivorce ? DIVORCE_ACCOUNT : CIVIL_PARTNERSHIP_ACCOUNT);
        templateVars.put(APPLICATION_TYPE, isDivorce ? YOUR_DIVORCE : YOUR_CIVIL_PARTNERSHIP);
        templateVars.put(SERVICE, isDivorce ? DIVORCE_SERVICE : CIVIL_PARTNERSHIP_SERVICE);
        templateVars.put(GOV_UK_LINK, isDivorce ? DIVORCE_GOV_UK_LINK : CIVIL_PARTNERSHIP_GOV_UK_LINK);
        templateVars.put(CITIZENS_ADVICE_LINK, isDivorce ? DIVORCE_CITIZENS_ADVICE_LINK : CIVIL_PARTNERSHIP_CITIZENS_ADVICE_LINK);
        return templateVars;
    }

    private void setInviteVariables(Map<String, String> templateVars, CaseData caseData) {
        Map<String, String> configTemplateVars = emailTemplatesConfig.getTemplateVars();

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(FOR_YOUR_APPLICATION, FOR_YOUR_DIVORCE);
            templateVars.put(FOR_A_APPLICATION, FOR_A_DIVORCE);
            templateVars.put(APPLICATION_TYPE_PROGRESS, THE_DIVORCE_APPLICATION);
            templateVars.put(CREATE_ACCOUNT_LINK, configTemplateVars.get(RESPONDENT_SIGN_IN_DIVORCE_URL));
        } else {
            templateVars.put(FOR_YOUR_APPLICATION, TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(FOR_A_APPLICATION, TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(APPLICATION_TYPE_PROGRESS, APPLICATION_TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(CREATE_ACCOUNT_LINK, configTemplateVars.get(RESPONDENT_SIGN_IN_DISSOLUTION_URL));
        }

        templateVars.put(ACCESS_CODE, caseData.getCaseInvite().getAccessCode());
    }
}
