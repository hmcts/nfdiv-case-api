package uk.gov.hmcts.divorce.solicitor.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;

@Slf4j
@Component
public class SolicitorSubmittedNotification {

    private static final String NO_EMAIL_SENT_FOR_CASE =
        "No applicant solicitor email is provided so no email sent for case id : {}";
    private static final String SENDING_AMENDED_APPLICATION_EMAIL =
        "Sending amended application submitted notification to applicant solicitor for case id : {}";
    private static final String SENDING_APPLICATION_EMAIL =
        "Sending application submitted notification to applicant solicitor for case id : {}";

    @Autowired
    private NotificationService notificationService;

    public void send(final CaseData caseData, final Long caseId) {

        final Solicitor solicitor = caseData.getApplicant1().getSolicitor();
        final HashMap<String, String> templateVars = new HashMap<>();

        templateVars.put(APPLICATION_REFERENCE, formatId(caseId));
        templateVars.put(FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateVars.put(LAST_NAME, caseData.getApplicant1().getLastName());

        if (solicitor != null && isNotEmpty(solicitor.getEmail())) {

            final EmailTemplateName templateName;
            final String logMessage;
            if (caseData.isAmendedCase()) {
                templateName = SOL_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED;
                logMessage = SENDING_AMENDED_APPLICATION_EMAIL;

            } else {
                templateName = SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
                logMessage = SENDING_APPLICATION_EMAIL;
            }

            notificationService.sendEmail(
                solicitor.getEmail(),
                templateName,
                templateVars,
                caseData.getApplicant1().getLanguagePreference());

            log.info(logMessage, caseId);

        } else {
            log.info(NO_EMAIL_SENT_FOR_CASE, caseId);
        }
    }
}
