package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAID;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAID_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR_IT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_ACTION_REQUIRED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;

@Component
@Slf4j
public class Applicant2ApprovedNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(caseData, caseData.getApplicant1());
        templateVars.put(REMINDER_ACTION_REQUIRED, "Action required: you");

        if (caseData.getApplication().getHelpWithFees().getNeedHelp() == YesOrNo.NO) {
            templateVars.put(PAY_FOR, PAY_FOR);
            templateVars.put(PAID_FOR, PAID);
        } else {
            templateVars.put(PAY_FOR, "");
            templateVars.put(PAID_FOR, "");
        }

        log.info("Sending applicant 2 approved notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_APPLICANT2_APPROVED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(caseData, caseData.getApplicant2());
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getApplicant2ApprovedDueDate().toString());

        if (caseData.getApplication().getHelpWithFees().getNeedHelp() == YesOrNo.NO) {
            templateVars.put(PAY_FOR, PAY_FOR);
            templateVars.put(PAY_FOR_IT, PAY_FOR_IT);
            templateVars.put(PAID_FOR, PAID_FOR);
        } else {
            templateVars.put(PAY_FOR, "");
            templateVars.put(PAY_FOR_IT, "");
            templateVars.put(PAID_FOR, "");
        }

        log.info("Sending applicant 2 approved notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            JOINT_APPLICANT2_APPLICANT2_APPROVED,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
