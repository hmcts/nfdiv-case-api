package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NAME_UPDATED;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateApplicant1NameNotification implements ApplicantNotification {

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        log.info("Notifying Applicant 1 that name has been updated for case {}", caseId);
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            APPLICANT_NAME_UPDATED,
            commonContent.mainTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            caseId);
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        log.info("Notifying Applicant 1 solicitor that other applicant name has been updated for case {}", caseId);
        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            APPLICANT_NAME_UPDATED,
            commonContent.mainTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }
}
