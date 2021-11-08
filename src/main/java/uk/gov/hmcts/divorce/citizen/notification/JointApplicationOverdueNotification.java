package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.citizen.notification.Applicant2ApprovedNotification.PAYS_FEES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_OVERDUE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACTION_REQUIRED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NO;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YES;

@Component
@Slf4j
public class JointApplicationOverdueNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendApplicationNotReviewedEmail(CaseData caseData, Long id) {
        log.info("Sending notification to applicant 1 to notify them of overdue joint application: {}", id);

        Map<String, String> templateVars =
            commonContent.templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICATION_OVERDUE,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicationApprovedReminderToApplicant1(CaseData caseData, Long id) {
        log.info("Sending notification to applicant 1 to notify them that applicant 2 has reviewed the application: {}", id);

        Map<String, String> templateVars =
            commonContent.templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(PAYS_FEES, caseData.getApplication().isHelpWithFeesApplication() ? NO : YES);
        templateVars.put(ACTION_REQUIRED, NO);
        templateVars.put(IS_REMINDER, YES);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
