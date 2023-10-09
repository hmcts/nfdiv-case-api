package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.POST_INFORMATION_TO_COURT;

@Component
@Slf4j
public class PostInformationToCourtNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {
        log.info("Notifying applicant 1 that they must post information to the court: {}", caseId);
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            POST_INFORMATION_TO_COURT,
            commonContent.conditionalOrderTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 (joint application) that they must post information to the court: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                POST_INFORMATION_TO_COURT,
                commonContent.conditionalOrderTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }
}
