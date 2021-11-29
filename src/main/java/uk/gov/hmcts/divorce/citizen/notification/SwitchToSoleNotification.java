package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ENDED;

@Component
@Slf4j
public class SwitchToSoleNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendApplicant1SwitchToSoleNotificationToApplicant1(CaseData caseData, Long id) {
        log.info("Sending applicant 1 switch to sole notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            APPLICANT_SWITCH_TO_SOLE,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicant1SwitchToSoleNotificationToApplicant2(CaseData caseData, Long id) {
        log.info("Sending applicant 1 switch to sole notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            JOINT_APPLICATION_ENDED,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicant2SwitchToSoleNotificationToApplicant1(CaseData caseData, Long id) {
        log.info("Sending applicant 2 switch to sole notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICATION_ENDED,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendApplicant2SwitchToSoleNotificationToApplicant2(CaseData caseData, Long id) {
        log.info("Sending applicant 2 switch to sole notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            APPLICANT_SWITCH_TO_SOLE,
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
