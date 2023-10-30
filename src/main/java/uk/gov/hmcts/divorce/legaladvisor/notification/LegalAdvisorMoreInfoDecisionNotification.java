package uk.gov.hmcts.divorce.legaladvisor.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderRefusalDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_REFUSED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;

@Component
@RequiredArgsConstructor
@Slf4j
public class LegalAdvisorMoreInfoDecisionNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;
    private final LetterPrinter letterPrinter;
    private final ConditionalOrderRefusalDocumentPack conditionalOrderRefusalDocumentPack;

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
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Sending CO refused notification to applicant 1 solicitor as required more info for case : {}", caseId);

        Applicant applicant = caseData.getApplicant1();

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_CO_REFUSED_SOLE_JOINT,
            commonContent.getCoRefusedSolicitorTemplateVars(caseData, caseId, applicant, MORE_INFO),
            ENGLISH,
            caseId
        );

        log.info("Successfully sent CO refused notification to applicant 1 solicitor as required more info for case : {}", caseId);
    }


    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Notifying applicant 1 offline that their conditional order is rejected - clarification needed: {}", caseId);
        var documentPack = conditionalOrderRefusalDocumentPack.getDocumentPack(caseData, caseData.getApplicant1());
        letterPrinter.sendLetters(
            caseData,
            caseId,
            caseData.getApplicant1(),
            documentPack,
            conditionalOrderRefusalDocumentPack.getLetterId());
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        final Map<String, String> templateVars = commonContent.conditionalOrderTemplateVars(caseData, caseId, applicant2, applicant1);

        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending Conditional order refused notification to applicant 2 for case : {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                CITIZEN_CONDITIONAL_ORDER_REFUSED,
                templateVars,
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        log.info("Sending CO refused notification to applicant 2 solicitor as required more info for case : {}", caseId);

        Applicant applicant = caseData.getApplicant2();

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_CO_REFUSED_SOLE_JOINT,
            commonContent.getCoRefusedSolicitorTemplateVars(caseData, caseId, applicant, MORE_INFO),
            ENGLISH,
            caseId
        );

        log.info("Successfully sent CO refused notification to applicant 2 solicitor as required more info for case : {}", caseId);
    }

    @Override
    public void sendToApplicant2Offline(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 offline that their conditional order is rejected - clarification needed: {}", caseId);
            var documentPack = conditionalOrderRefusalDocumentPack.getDocumentPack(caseData, caseData.getApplicant2());
            letterPrinter.sendLetters(
                caseData,
                caseId,
                caseData.getApplicant2(),
                documentPack,
                conditionalOrderRefusalDocumentPack.getLetterId());
        }
    }
}
