package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_OVERDUE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAID_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_ACTION_REQUIRED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_END_CIVIL_PARTNERSHIP;

@Component
@Slf4j
public class JointApplicationOverdueNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendApplicationNotReviewedEmail(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), FOR_DIVORCE);
        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), TO_END_CIVIL_PARTNERSHIP);
        }

        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getDueDate().toString());

        log.info("Sending notification to applicant 1 to notify them of overdue joint application: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICATION_OVERDUE,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicationApprovedReminderToApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), FOR_DIVORCE);
        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), TO_END_CIVIL_PARTNERSHIP);
        }

        templateVars.put(REMINDER_ACTION_REQUIRED, REMINDER_ACTION_REQUIRED);

        if (caseData.getApplication().isHelpWithFeesApplication()) {
            templateVars.put(PAY_FOR, "");
            templateVars.put(PAID_FOR, "");
        } else {
            templateVars.put(PAY_FOR, PAY_FOR);
            templateVars.put(PAID_FOR, PAID_FOR);
        }

        log.info("Sending notification to applicant 1 to notify them that applicant 2 has reviewed the application: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
