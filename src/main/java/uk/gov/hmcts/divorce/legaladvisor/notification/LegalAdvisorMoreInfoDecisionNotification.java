package uk.gov.hmcts.divorce.legaladvisor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_REFUSED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CLARIFICATION_SUBMITTED;

@Component
@Slf4j
public class LegalAdvisorMoreInfoDecisionNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {

        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        final Map<String, String> templateVars = commonContent.conditionalOrderTemplateVars(caseData, caseId, applicant1, applicant2);

        log.info("Sending Conditional order refused notification to applicant 1 for case : {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_CONDITIONAL_ORDER_REFUSED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        final Map<String, String> templateVars = commonContent.conditionalOrderTemplateVars(caseData, caseId, applicant2, applicant1);

        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending Conditional order refused notification to applicant 2 for case : {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                CITIZEN_CONDITIONAL_ORDER_REFUSED,
                templateVars,
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);

        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());

        log.info("Sending Clarification Submitted notification for case : {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLICITOR_CLARIFICATION_SUBMITTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );

        log.info("Successfully sent Clarification Submitted notification for case : {}", caseId);
    }
}
