package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.BAILIFF_SERVICE_APPLICATION_AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.BAILIFF_SERVICE_APPLICATION_SUBMITTED;

@Component
@Slf4j
@RequiredArgsConstructor
public class BailiffServiceApplicationSubmittedNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        log.info("Sending bailiff service application submitted notification to applicant 1 on case id {}", caseId);

        AlternativeService serviceApplication = caseData.getAlternativeService();
        boolean awaitingDocuments = YesOrNo.NO.equals(serviceApplication.getServiceApplicationDocsUploadedPreSubmission());

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            awaitingDocuments ? BAILIFF_SERVICE_APPLICATION_AWAITING_DOCUMENTS : BAILIFF_SERVICE_APPLICATION_SUBMITTED,
            templateVars(caseData, caseId, caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant) {
        return commonContent.serviceApplicationTemplateVars(caseData, id, applicant);
    }
}
