package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CERTIFICATE_OF_SERVICE_EVIDENCE_NOTIFICATION;

@Component
@RequiredArgsConstructor
public class SendCertificateOfServiceNotification {
    private final NotificationService notificationService;

    private final CommonContent commonContent;

    public void notifyApplicant(CaseData caseData, Long caseId) {
        final var templateContent = commonContent.mainTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2());

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CERTIFICATE_OF_SERVICE_EVIDENCE_NOTIFICATION,
            templateContent,
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }
}
