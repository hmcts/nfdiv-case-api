package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;

@Component
@Slf4j
public class Applicant2RemindAwaitingJointFinalOrderNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FinalOrderNotificationCommonContent finalOrderNotificationCommonContent;

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending reminder to Applicant 2 informing them that other party has applied for final order: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER,
                finalOrderNotificationCommonContent
                    .jointApplicantTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1(), true),
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }
}
