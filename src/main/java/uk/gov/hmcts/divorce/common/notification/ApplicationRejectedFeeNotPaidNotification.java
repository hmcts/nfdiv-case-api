package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_REJECTED_FEE_NOT_PAID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationRejectedFeeNotPaidNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        log.info("Sending application rejected notification to applicant 1 for case : {}", caseId);

        Applicant applicant = caseData.getApplicant1();

        var templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant, caseData.getApplicant2());

        notificationService.sendEmail(
            applicant.getEmail(),
            APPLICATION_REJECTED_FEE_NOT_PAID,
            templateContent,
            applicant.getLanguagePreference(),
            caseId);
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending application rejected notification to applicant 2 for case : {}", caseId);

            Applicant applicant2 = caseData.getApplicant2();

            var templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant2, caseData.getApplicant1());

            notificationService.sendEmail(
                applicant2.getEmail(),
                APPLICATION_REJECTED_FEE_NOT_PAID,
                templateContent,
                applicant2.getLanguagePreference(),
                caseId);
        }
    }
}
