package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_NO_EMAIL_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NO;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YES;

@Component
@Slf4j
public class ApplicationIssuedNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void sendToSoleApplicant1(CaseData caseData, Long id) {
        log.info("Sending sole application issued notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_APPLICATION_ACCEPTED,
            soleApplicant1TemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToSoleRespondent(CaseData caseData, Long id) {
        log.info("Sending sole application issued notification to respondent for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            SOLE_RESPONDENT_APPLICATION_ACCEPTED,
            soleRespondentTemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendReminderToSoleRespondent(CaseData caseData, Long id) {
        log.info("Sending reminder to respondent to register for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            SOLE_RESPONDENT_APPLICATION_ACCEPTED,
            reminderToSoleRespondentTemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendPartnerNotRespondedToSoleApplicant(CaseData caseData, Long id) {
        log.info("Sending the respondent has not responded notification to the applicant for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED,
            partnerNotRespondedToSoleApplicantTemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToJointApplicant1(CaseData caseData, Long id) {
        log.info("Sending joint application issued notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICATION_ACCEPTED,
            jointApplicantTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToJointApplicant2(CaseData caseData, Long id) {
        log.info("Sending joint application issued notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICATION_ACCEPTED,
            jointApplicantTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void notifyApplicantOfServiceToOverseasRespondent(CaseData caseData, Long id) {
        log.info("Notifying sole applicant of application issue (case {}) to overseas respondent", id);

        final boolean hasEmail = caseData.getCaseInvite().getApplicant2InviteEmailAddress() != null
            && !caseData.getCaseInvite().getApplicant2InviteEmailAddress().isEmpty();
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            hasEmail ? OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED : OVERSEAS_RESPONDENT_NO_EMAIL_APPLICATION_ISSUED,
            overseasRespondentTemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    private Map<String, String> soleApplicant1TemplateVars(final CaseData caseData, Long id) {
        final Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getApplication().getIssueDate().plusDays(14).format(DATE_TIME_FORMATTER));
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY,
            emailTemplatesConfig.getTemplateVars().get(isDivorce(caseData) ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    private Map<String, String> soleRespondentTemplateVars(final CaseData caseData, Long id) {
        final Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        templateVars.put(IS_REMINDER,  NO);
        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getApplication().getIssueDate().plusDays(16).format(DATE_TIME_FORMATTER));
        templateVars.put(
            CREATE_ACCOUNT_LINK,
            emailTemplatesConfig.getTemplateVars()
                .get(isDivorce(caseData) ? RESPONDENT_SIGN_IN_DIVORCE_URL : RESPONDENT_SIGN_IN_DISSOLUTION_URL)
        );
        templateVars.put(ACCESS_CODE, caseData.getCaseInvite().getAccessCode());
        return templateVars;
    }

    private Map<String, String> reminderToSoleRespondentTemplateVars(final CaseData caseData, Long id) {
        final Map<String, String> templateVars = soleRespondentTemplateVars(caseData, id);
        templateVars.put(IS_REMINDER,  YES);
        return templateVars;
    }

    private Map<String, String> partnerNotRespondedToSoleApplicantTemplateVars(final CaseData caseData, Long id) {
        final Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY,
            emailTemplatesConfig.getTemplateVars().get(isDivorce(caseData) ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));
        return templateVars;
    }

    private Map<String, String> jointApplicantTemplateVars(final CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        final Map<String, String> templateVars = commonTemplateVars(caseData, id, applicant, partner);
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY,
            emailTemplatesConfig.getTemplateVars().get(isDivorce(caseData) ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    private Map<String, String> overseasRespondentTemplateVars(final CaseData caseData, Long id) {
        final Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getApplication().getIssueDate().plusDays(28).format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    private Map<String, String> commonTemplateVars(final CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        final Map<String, String> templateVars = new HashMap<>();
        templateVars.put(IS_DIVORCE,  isDivorce(caseData) ? YES : NO);
        templateVars.put(IS_DISSOLUTION,  isDivorce(caseData) ? NO : YES);
        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(PARTNER, commonContent.getPartner(caseData, partner));
        templateVars.put(COURT_EMAIL, emailTemplatesConfig.getTemplateVars().get(DISSOLUTION_COURT_EMAIL));
        return templateVars;
    }
}
