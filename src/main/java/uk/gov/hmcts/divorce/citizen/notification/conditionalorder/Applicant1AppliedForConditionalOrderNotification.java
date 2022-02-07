package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class Applicant1AppliedForConditionalOrderNotification
    extends AppliedForConditionalOrderNotification
    implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 that their conditional order application has been submitted: {}", id);
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            caseData.getApplicationType().isSole() ? CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER : JOINT_APPLIED_FOR_CONDITIONAL_ORDER,
            templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(), APPLICANT1),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData data, final Long id) {
        if (!data.getApplicationType().isSole() && !alreadyApplied(data, APPLICANT2)) {
            log.info("Notifying applicant 2 that their partner has submitted a conditional order application: {}", id);
            notificationService.sendEmail(
                data.getApplicant2().getEmail(),
                JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER,
                partnerTemplateVars(data, id, data.getApplicant2(), data.getApplicant1(), APPLICANT1),
                data.getApplicant2().getLanguagePreference()
            );
        }
    }
}
