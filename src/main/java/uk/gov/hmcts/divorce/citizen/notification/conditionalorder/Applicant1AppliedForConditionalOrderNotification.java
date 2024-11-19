package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.AppliedForConditionalOrderDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT1_SOLICITOR_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class Applicant1AppliedForConditionalOrderNotification
    extends AppliedForConditionalOrderNotification
    implements ApplicantNotification {

    private final NotificationService notificationService;
    private final AppliedForConditionalOrderDocumentPack appliedForConditionalOrderDocumentPack;
    private final LetterPrinter letterPrinter;

    public Applicant1AppliedForConditionalOrderNotification(
        CommonContent commonContent,
        NotificationService notificationService,
        AppliedForConditionalOrderDocumentPack appliedForConditionalOrderDocumentPack,
        LetterPrinter letterPrinter) {
        super(commonContent);
        this.appliedForConditionalOrderDocumentPack = appliedForConditionalOrderDocumentPack;
        this.notificationService = notificationService;
        this.letterPrinter = letterPrinter;
    }

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {

        EmailTemplateName templateName;

        if (caseData.getApplicationType().isSole()) {
            templateName = CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
            log.info("Notifying applicant that their conditional order application has been submitted for case: {}, with template: {}",
                caseId, templateName);
        } else if (alreadyApplied(caseData, APPLICANT2)) {
            templateName = JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
            log.info("Notifying applicant 1 that both applicants have submitted their conditional order applications for case: {}, "
                + "with template: {}", caseId, templateName);
        } else {
            templateName = JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
            log.info("Notifying applicant 1 that their conditional order application has been submitted for case: {}, with template: {}",
                caseId, templateName);
        }

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            templateName,
            templateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2(), APPLICANT1),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        if (caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant's solicitor that their conditional order application has been submitted: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                APPLICANT1_SOLICITOR_APPLIED_FOR_CONDITIONAL_ORDER,
                solicitorTemplateVars(caseData, id, caseData.getApplicant1().getSolicitor()),
                caseData.getApplicant1().getLanguagePreference(),
                id
            );
        } else {
            log.info("Notifying applicant 1 solicitor that their conditional order application has been submitted: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER,
                solicitorTemplateVars(caseData, id, caseData.getApplicant1(), APPLICANT1),
                ENGLISH,
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        log.info("Sending You have applied for a Conditional Order letter to Applicant 1: {}", caseId);
        Applicant applicant1 = caseData.getApplicant1();
        var documentPackInfo = appliedForConditionalOrderDocumentPack.getDocumentPack(caseData, applicant1);
        letterPrinter.sendLetters(caseData, caseId, applicant1, documentPackInfo, appliedForConditionalOrderDocumentPack.getLetterId());
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()) {

            EmailTemplateName templateName;
            Map<String, String> templateMap;

            if (alreadyApplied(caseData, APPLICANT2)) {
                templateName = JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
                templateMap = templateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1(), APPLICANT1);
                log.info("Notifying applicant 2 that both applicants have submitted their conditional order applications for case: {}, "
                    + "with template: {}", caseId, templateName);
            } else {
                templateName = JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
                templateMap = partnerTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1(), APPLICANT1);
                log.info("Notifying applicant 2 that their partner has submitted a conditional order application for case: {}, "
                    + "with template: {}", caseId, templateName);
            }

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                templateName,
                templateMap,
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Sending You have applied for a Conditional Order letter to Applicant 2: {}", caseId);
            Applicant applicant2 = caseData.getApplicant2();
            var documentPackInfo = appliedForConditionalOrderDocumentPack.getDocumentPack(caseData, applicant2);
            letterPrinter.sendLetters(caseData, caseId, applicant2, documentPackInfo, appliedForConditionalOrderDocumentPack.getLetterId());
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole() && !alreadyApplied(caseData, APPLICANT2)) {
            log.info("Notifying applicant 2 solicitor that other party has applied for Conditional Order for case: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_CONDITIONAL_ORDER,
                solicitorTemplateVars(caseData, id, caseData.getApplicant2(), APPLICANT1),
                ENGLISH,
                id
            );
        }
    }
}
