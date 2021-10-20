package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_YOUR_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_APPLICATION_VALUE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_END_CIVIL_PARTNERSHIP;

@Component
@Slf4j
public class ApplicationSentForReviewApplicant2Notification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant2(), caseData.getApplicant1());

        setDefaultVariables(templateVars, caseData, id);
        if (caseData.getDivorceOrDissolution().isDivorce()) {
            setDivorceVariables(templateVars);
        } else {
            setDissolutionVariables(templateVars);
        }

        log.info("Sending application sent for review notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendReminder(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant2(), caseData.getApplicant1());

        setDefaultVariables(templateVars, caseData, id);
        templateVars.put(REMINDER_APPLICATION, REMINDER_APPLICATION_VALUE);
        if (caseData.getDivorceOrDissolution().isDivorce()) {
            setDivorceVariables(templateVars);
        } else {
            setDissolutionVariables(templateVars);
        }

        log.info("Sending reminder to applicant 2 to review case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    private void setDefaultVariables(Map<String, String> templateVars, CaseData caseData, Long id) {
        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(ACCESS_CODE, caseData.getCaseInvite().getAccessCode());
        templateVars.put(REMINDER_APPLICATION, StringUtils.capitalize(APPLICATION));
    }

    private void setDivorceVariables(Map<String, String> templateVars) {
        templateVars.put(APPLICATION, "a " + DIVORCE_APPLICATION);
        templateVars.put(ACCOUNT, DIVORCE_ACCOUNT);
        templateVars.put(FOR_YOUR_APPLICATION, FOR_YOUR_DIVORCE);

        Map<String, String> configTemplateVars = emailTemplatesConfig.getTemplateVars();
        templateVars.put(CREATE_ACCOUNT_LINK, configTemplateVars.get(APPLICANT_2_SIGN_IN_DIVORCE_URL));
    }

    private void setDissolutionVariables(Map<String, String> templateVars) {
        templateVars.put(APPLICATION, "an " + APPLICATION_TO_END_CIVIL_PARTNERSHIP);
        templateVars.put(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT);
        templateVars.put(FOR_YOUR_APPLICATION, TO_END_CIVIL_PARTNERSHIP);

        Map<String, String> configTemplateVars = emailTemplatesConfig.getTemplateVars();
        templateVars.put(CREATE_ACCOUNT_LINK, configTemplateVars.get(APPLICANT_2_SIGN_IN_DISSOLUTION_URL));
    }
}
