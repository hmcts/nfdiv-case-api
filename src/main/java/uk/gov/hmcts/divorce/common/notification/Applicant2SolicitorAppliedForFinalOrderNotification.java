package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_2_SOLICITOR_APPLIED_FOR_FINAL_ORDER;

@Component
@Slf4j
public class Applicant2SolicitorAppliedForFinalOrderNotification implements ApplicantNotification {

    @Autowired
    private FinalOrderNotificationCommonContent finalOrderNotificationCommonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Notifying applicant 2 solicitor that their final order application has been submitted: {}", caseId);
        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            APPLICANT_2_SOLICITOR_APPLIED_FOR_FINAL_ORDER,
            getTemplateVars(caseData, caseId),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> getTemplateVars(final CaseData caseData, Long caseId) {
        return finalOrderNotificationCommonContent
            .applicant2SolicitorTemplateVars(caseData, caseId);
    }
}
