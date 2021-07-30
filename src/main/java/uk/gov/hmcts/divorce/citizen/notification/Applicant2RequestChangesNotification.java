package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_COMMENTS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SWITCH_TO_SOLE_APPLICATION_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SWITCH_TO_SOLE_APPLICATION_DIVORCE_URL;

@Component
@Slf4j
public class Applicant2RequestChangesNotification {

    private static final String FOR_DIVORCE = "for divorce";
    private static final String END_CIVIL_PARTNERSHIP = "to end your civil partnership";
    private static final String SIGN_IN_LINK = "sign in to edit your application link";
    private static final String END_JOINT_APPLICATION_LINK = "end joint application link";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void sendToApplicant1(CaseData caseData, Long id) {
        Map<String, String> configTemplateVars = emailTemplatesConfig.getTemplateVars();
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        templateVars.put(PARTNER, commonContent.getTheirPartner(caseData, caseData.getApplicant2()));
        templateVars.put(APPLICANT_2_COMMENTS, caseData.getApplication().getApplicant2ExplainsApplicant1IncorrectInformation());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), FOR_DIVORCE);
            templateVars.put(APPLICATION_TYPE.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION);
            templateVars.put(SIGN_IN_LINK, configTemplateVars.get(SIGN_IN_DIVORCE_URL));
            templateVars.put(END_JOINT_APPLICATION_LINK, configTemplateVars.get(SWITCH_TO_SOLE_APPLICATION_DIVORCE_URL));
        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), END_CIVIL_PARTNERSHIP);
            templateVars.put(APPLICATION_TYPE.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(SIGN_IN_LINK, configTemplateVars.get(SIGN_IN_DISSOLUTION_URL));
            templateVars.put(END_JOINT_APPLICATION_LINK, configTemplateVars.get(SWITCH_TO_SOLE_APPLICATION_DISSOLUTION_URL));
        }

        log.info("Sending notification to applicant 1 to request changes: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);
        templateVars.put(FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateVars.put(LAST_NAME, caseData.getApplicant2().getLastName());
        templateVars.put(PARTNER, commonContent.getTheirPartner(caseData, caseData.getApplicant1()));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), FOR_DIVORCE);
        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), END_CIVIL_PARTNERSHIP);
        }

        log.info("Sending notification to applicant 2 to confirm their request for changes: {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICANT2_REQUEST_CHANGES,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
