package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.FormatUtil.dateTimeFormatter;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;

@Component
@Slf4j
public class ApplicationSentForReviewApplicant2Notification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(dateTimeFormatter));
        templateVars.put(PARTNER, commonContent.getPartner(caseData));
        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(ACCESS_CODE, caseData.getInvitePin());
        templateVars.put("Reminder: application / Application ", "Application");

//        templateVars.put(ACCOUNT_LINK, "((link_to_login))");   // not been made yet
        Map<String, String> configTemplateVars = emailTemplatesConfig.getTemplateVars();
        String signInUrlKey = caseData.getDivorceOrDissolution().isDivorce() ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL;
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY, configTemplateVars.get(signInUrlKey));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION, "a" + DIVORCE_APPLICATION);
            templateVars.put(ACCOUNT, DIVORCE_ACCOUNT);
            templateVars.put(" for your divorce/ to end your civil partnership", " for your divorce");
        } else {
            templateVars.put(APPLICATION, "an" + APPLICATION_TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT);
            templateVars.put(" for your divorce/ to end your civil partnership", "to end your civil partnership");
        }

        log.info("Sending application sent for review notification for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
