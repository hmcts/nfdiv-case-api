package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;

@Component
@Slf4j
public class Applicant2RequestChangesNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);
        templateVars.put(PARTNER, commonContent.getTheirPartner(caseData, caseData.getApplicant1()));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), "for divorce");
        } else {
            templateVars.put(APPLICATION.toLowerCase(Locale.ROOT), "to end your civil partnership");
        }

        log.info("Sending notification to applicant 2 to confirm their request for changes: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            JOINT_APPLICANT2_REQUEST_CHANGES,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
