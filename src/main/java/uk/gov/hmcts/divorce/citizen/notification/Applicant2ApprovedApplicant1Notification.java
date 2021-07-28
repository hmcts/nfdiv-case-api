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
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_ACTION_REQUIRED;

@Component
@Slf4j
public class Applicant2ApprovedApplicant1Notification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        templateVars.put(PARTNER, commonContent.getTheirPartner(caseData, caseData.getApplicant2()));
        templateVars.put(REMINDER_ACTION_REQUIRED, "Action required: you");

        if (caseData.getApplication().getHelpWithFees().getNeedHelp() != YesOrNo.YES) {
            templateVars.put("and pay for", "and pay for");
            templateVars.put("and paid for", "and paid for");
        }
        else {
            templateVars.put("and pay for", "");
            templateVars.put("and paid for", "");
        }

        log.info("Sending application sent for review notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_APPLICANT2_APPROVED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
