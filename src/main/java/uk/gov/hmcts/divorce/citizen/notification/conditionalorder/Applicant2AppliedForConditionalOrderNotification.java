package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class Applicant2AppliedForConditionalOrderNotification
    extends AppliedForConditionalOrderNotification
    implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        EmailTemplateName templateName;
        Map<String, String> templateMap;

        if (alreadyApplied(caseData, APPLICANT1)) {
            log.info("Notifying applicant 1 that both applicants has submitted their conditional order application: {}", id);
            templateName = JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
            templateMap = templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(), APPLICANT2);
        } else {
            log.info("Notifying applicant 1 that their partner has submitted a conditional order application: {}", id);
            templateName = JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
            templateMap = partnerTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(), APPLICANT2);
        }

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            templateName,
            templateMap,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {

        EmailTemplateName templateName;

        if (alreadyApplied(caseData, APPLICANT1)) {
            log.info("Notifying applicant 2 that both applicants has submitted their conditional order application: {}", id);
            templateName = JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
        } else {
            log.info("Notifying applicant 2 that their conditional order application has been submitted: {}", id);
            templateName = JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
        }

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            templateName,
            templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1(), APPLICANT2),
            caseData.getApplicant2().getLanguagePreference()
        );
    }
}
