package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_OTHER_PARTY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT_SOLICITOR;

@Component
@Slf4j
public class GeneralEmailNotification {

    public static final String GENERAL_EMAIL_DETAILS = "general email details";
    public static final String GENERAL_OTHER_RECIPIENT_NAME = "general other recipient name";

    @Autowired
    private NotificationService notificationService;

    public void send(final CaseData caseData, final Long caseId) {
        log.info("Sending General Email Notification for case id: {}", caseId);

        String emailTo = null;
        EmailTemplateName templateId;

        Map<String, String> templateVars = templateVars(caseData, caseId);

        if (APPLICANT.equals(caseData.getGeneralEmail().getGeneralEmailParties())) {
            if (caseData.getApplicant1().isRepresented()) {
                log.info("Sending General Email Notification to petitioner solicitor for case id: {}", caseId);
                emailTo = caseData.getApplicant1().getSolicitor().getEmail();
                templateId = GENERAL_EMAIL_PETITIONER_SOLICITOR;
                templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
            } else {
                log.info("Sending General Email Notification to petitioner for case id: {}", caseId);
                emailTo = caseData.getApplicant1().getEmail();
                templateId = GENERAL_EMAIL_PETITIONER;
            }
        } else if (RESPONDENT.equals(caseData.getGeneralEmail().getGeneralEmailParties())) {
            if (caseData.getApplicant2().isRepresented()) {
                log.info("Sending General Email Notification to respondent solicitor for case id: {}", caseId);
                emailTo = caseData.getApplicant2().getSolicitor().getEmail();
                templateId = GENERAL_EMAIL_RESPONDENT_SOLICITOR;
                templateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
            } else {
                log.info("Sending General Email Notification to respondent for case id: {}", caseId);
                emailTo = caseData.getApplicant2().getEmail();
                templateId = GENERAL_EMAIL_RESPONDENT;
            }
        } else {
            log.info("Sending General Email Notification to other party for case id: {}", caseId);
            emailTo = caseData.getGeneralEmail().getGeneralEmailOtherRecipientEmail();
            templateId = GENERAL_EMAIL_OTHER_PARTY;
        }

        if (null == emailTo) {
            log.info("Email address is not available for template id {} and case {} ", templateId, caseId);
        } else {
            notificationService.sendEmail(
                emailTo,
                templateId,
                templateVars,
                ENGLISH
            );
            log.info("Successfully sent general email notification for case id: {}", caseId);
        }
    }

    private Map<String, String> templateVars(final CaseData caseData, final Long caseId) {
        final Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICANT_NAME, caseData.getApplication().getMarriageDetails().getApplicant1Name());
        templateVars.put(RESPONDENT_NAME, caseData.getApplication().getMarriageDetails().getApplicant2Name());
        templateVars.put(APPLICATION_REFERENCE, String.valueOf(caseId));
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, caseData.getGeneralEmail().getGeneralEmailOtherRecipientName());
        templateVars.put(GENERAL_EMAIL_DETAILS, caseData.getGeneralEmail().getGeneralEmailDetails());
        return templateVars;
    }
}
