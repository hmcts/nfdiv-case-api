package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;

@Component
@Slf4j
public class Applicant1ApplyForConditionalOrderNotification {

    public static final String JOINT_CONDITIONAL_ORDER = "Your partner has also received this notification."
        + " They need to apply for a conditional order too because this is a joint application.";
    public static final String PARTNER_JOINT = "Your %s has also received this notification."
        + " They need to apply for a conditional order too because this is a joint application.";
    public static final String YOUR_APPLICATION = "get a divorce / end your civil partnership";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        templateVars.put(APPLICATION_REFERENCE, String.valueOf(id));
        templateVars.put(YOUR_APPLICATION,
            caseData.getDivorceOrDissolution().isDivorce() ? "get a divorce" : "end your civil partnership");

        if (caseData.getApplicationType().isSole()) {
            templateVars.put(JOINT_CONDITIONAL_ORDER, "");
        } else {
            templateVars.put(JOINT_CONDITIONAL_ORDER,
                String.format(PARTNER_JOINT, commonContent.getTheirPartner(caseData, caseData.getApplicant2())));
        }

        log.info("Sending notification to applicant 1 to notify them that they can apply for a conditional order: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
