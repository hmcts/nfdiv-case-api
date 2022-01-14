package uk.gov.hmcts.divorce.legaladvisor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CLARIFICATION_SUBMITTED;

@Component
@Slf4j
public class LegalAdvisorClarificationSubmittedNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        if (caseData.getApplication().isSolicitorApplication()) {
            final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);

            templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());

            log.info("Sending Clarification Submitted notification for case : {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                SOLICITOR_CLARIFICATION_SUBMITTED,
                templateVars,
                caseData.getApplicant1().getLanguagePreference()
            );

            log.info("Successfully sent Clarification Submitted notification for case : {}", caseId);
        }
    }
}
