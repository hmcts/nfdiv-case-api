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
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAID;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAID_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_ACTION_REQUIRED;

@Component
@Slf4j
public class Applicant2ApprovedApplicant1Notification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(caseData, caseData.getApplicant1());

        templateVars.put(REMINDER_ACTION_REQUIRED, "Action required: you");

        if (caseData.getApplication().getHelpWithFees().getNeedHelp() != YesOrNo.YES) {
            templateVars.put(PAY_FOR, PAY_FOR);
            templateVars.put(PAID_FOR, PAID);
        }
        else {
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
}
