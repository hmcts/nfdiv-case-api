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
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_DIVORCE;

@Component
@Slf4j
public class JointApplicationIssueNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        log.info("Sending application issued notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICATION_ACCEPTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        Map<String, String> templateVars = setTemplateVariables(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        log.info("Sending application issued notification to applicant 2 for case : {}", id);

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
