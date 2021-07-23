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

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.*;

@Component
@Slf4j
public class Applicant2RequestChangesNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), "a " + DIVORCE_APPLICATION);
        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), "an " + APPLICATION_TO_END_CIVIL_PARTNERSHIP);
        }

        log.info("Sending notification to applicant 1 that applicant 2 requests changes for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICANT2_REQUEST_CHANGES,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
