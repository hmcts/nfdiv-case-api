package uk.gov.hmcts.divorce.legaladvisor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_SUCCESSFUL;

@Component
@Slf4j
public class LegalAdvisorGeneralReferralDecisionNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        final Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant1, applicant2);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            GENERAL_APPLICATION_SUCCESSFUL,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        final Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant1, applicant2);

        templateVars.put(FIRST_NAME, applicant2.getFirstName());
        templateVars.put(LAST_NAME, applicant2.getLastName());

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            GENERAL_APPLICATION_SUCCESSFUL,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
