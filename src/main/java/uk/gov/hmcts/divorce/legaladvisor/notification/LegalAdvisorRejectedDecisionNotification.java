package uk.gov.hmcts.divorce.legaladvisor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.legaladvisor.service.printer.AwaitingAmendedApplicationPrinter;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;

@Component
@Slf4j
public class LegalAdvisorRejectedDecisionNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private AwaitingAmendedApplicationPrinter awaitingAmendedApplicationPrinter;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {

        final Map<String, String> templateVars = commonContent.conditionalOrderTemplateVars(caseData, caseId, caseData.getApplicant1(),
            caseData.getApplicant2());

        log.info("Sending Conditional order refused for amendment notification to applicant 1 for case : {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        if (!caseData.getApplicationType().isSole()) {

            final Map<String, String> templateVars = commonContent.conditionalOrderTemplateVars(caseData, caseId, caseData.getApplicant2(),
                caseData.getApplicant1());

            log.info("Sending Conditional order refused for amendment notification to applicant 2 for case : {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT,
                templateVars,
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Sending CO refused notification to applicant 1 solicitor as some amendments needed for case : {}", caseId);

        Applicant applicant = caseData.getApplicant1();

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_CO_REFUSED_SOLE_JOINT,
            commonContent.getCoRefusedSolicitorTemplateVars(caseData, caseId, applicant, REJECT),
            ENGLISH
        );

        log.info("Successfully sent CO refused notification to applicant 1 solicitor as some amendments needed for case : {}", caseId);
    }

    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        log.info("Notifying applicant 1 offline that their conditional order is rejected: {}", caseId);
        awaitingAmendedApplicationPrinter.sendLetters(caseData, caseId, caseData.getApplicant1());
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Sending CO refused notification to applicant 2 solicitor as some amendments needed for case : {}", caseId);

        Applicant applicant = caseData.getApplicant2();

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_CO_REFUSED_SOLE_JOINT,
            commonContent.getCoRefusedSolicitorTemplateVars(caseData, caseId, applicant, REJECT),
            ENGLISH
        );

        log.info("Successfully sent CO refused notification to applicant 2 solicitor as some amendments needed for case : {}", caseId);
    }

    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 offline that their conditional order is rejected: {}", caseId);
            awaitingAmendedApplicationPrinter.sendLetters(caseData, caseId, caseData.getApplicant2());
        }
    }
}
