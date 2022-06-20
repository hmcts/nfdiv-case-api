package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class Applicant2AppliedForConditionalOrderNotification
    extends AppliedForConditionalOrderNotification
    implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        if (!alreadyApplied(caseData, APPLICANT1)) {
            log.info("Notifying applicant 1 that their partner has submitted a conditional order application: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER,
                partnerTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(), APPLICANT2),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 2 that their conditional order application has been submitted: {}", id);
        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            JOINT_APPLIED_FOR_CONDITIONAL_ORDER,
            templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1(), APPLICANT2),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()
            && StringUtils.isNotBlank(caseData.getApplicant2().getSolicitor().getEmail())) {
            log.info("Notifying applicant 2 solicitor that their conditional order application has been submitted: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                JOINT_SOLICITOR_APPLIED_FOR_CONDITIONAL_ORDER,
                solicitorTemplateVars(caseData, caseId, caseData.getApplicant2(), APPLICANT2),
                ENGLISH
            );
        }
    }
}
