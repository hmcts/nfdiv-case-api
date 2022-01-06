package uk.gov.hmcts.divorce.solicitor.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;

import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Slf4j
@Component
public class SolicitorSubmittedNotification implements ApplicantNotification {

    private static final String SENDING_AMENDED_APPLICATION_EMAIL =
        "Sending amended application submitted notification to applicant solicitor for case id : {}";
    private static final String SENDING_APPLICATION_EMAIL =
        "Sending application submitted notification to applicant solicitor for case id : {}";

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        final HashMap<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, formatId(caseId));
        templateVars.put(FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateVars.put(LAST_NAME, caseData.getApplicant1().getLastName());

        final EmailTemplateName templateName;
        final String logMessage;
        if (caseData.isAmendedCase()) {
            templateName = SOLE_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED;
            logMessage = SENDING_AMENDED_APPLICATION_EMAIL;

        } else {
            templateName = SOLE_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
            logMessage = SENDING_APPLICATION_EMAIL;
        }

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            templateName,
            templateVars,
            caseData.getApplicant1().getLanguagePreference());

        log.info(logMessage, caseId);
    }
}
