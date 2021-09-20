package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLY_FOR_CONDITIONAL_ORDER_LINK;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_END_CIVIL_PARTNERSHIP;

@Component
@Slf4j
public class Applicant1ApplyForConditionalOrderNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void sendToApplicant1(CaseData caseData, Long id) {
        Map<String, String> configTemplateVars = emailTemplatesConfig.getTemplateVars();
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        templateVars.put(APPLICATION_REFERENCE, String.valueOf(id));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), FOR_DIVORCE);
            templateVars.put(APPLICATION_TYPE.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION);
            templateVars.put(APPLY_FOR_CONDITIONAL_ORDER_LINK, configTemplateVars.get(SIGN_IN_DIVORCE_URL));
        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(APPLICATION_TYPE.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(APPLY_FOR_CONDITIONAL_ORDER_LINK, configTemplateVars.get(SIGN_IN_DISSOLUTION_URL));
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
