package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CourtOrderRegeneratedTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.CertificateOfEntitlementDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.documentpack.FinalOrderGrantedDocumentPack;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CITIZEN_COURT_ORDERS_REGENERATED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.COURT_ORDERS_REGENERATED_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.COURT_ORDERS_REGENERATED_SOLICITOR;

@RequiredArgsConstructor
@Component
@Slf4j
public class RegenerateCourtOrdersNotification implements ApplicantNotification {
    private final LetterPrinter letterPrinter;
    private final CertificateOfEntitlementDocumentPack certificateOfEntitlementDocPack;
    private final FinalOrderGrantedDocumentPack finalOrderGrantedDocPack;
    private final ConditionalOrderPronouncedDocumentPack conditionalOrderPronouncedDocPack;
    private final NotificationService notificationService;
    private final CommonContent commonContent;
    private final BulkPrintService bulkPrintService;
    private final CaseDataDocumentService caseDataDocumentService;
    private final CourtOrderRegeneratedTemplateContent courtOrderRegeneratedTemplateContent;

    public static final String LETTER_TYPE_COURT_ORDERS_REGENERATED = "court-order-regenerated";
    public static final String DOCUMENT_NAME_COURT_ORDERS_REGENERATED = "Court Orders Regenerated Letter";


    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 1 for case : {}", caseId);

        generateAndSendCourtOrdersRegeneratedLetter(caseData, caseId, true);

        if (isNotEmpty(caseData.getConditionalOrder().getCertificateOfEntitlementDocument())) {
            DocumentPackInfo certOfEntitlementDocPackInfo
                = certificateOfEntitlementDocPack.getDocumentPack(caseData, caseData.getApplicant1());

            letterPrinter.sendLetters(
                caseData,
                caseId,
                caseData.getApplicant1(),
                certOfEntitlementDocPackInfo,
                certificateOfEntitlementDocPack.getLetterId()
            );
        }

        if (caseData.getDocuments().getDocumentGeneratedWithType(FINAL_ORDER_GRANTED).isPresent()) {
            DocumentPackInfo finalOrderGrantedDocPackInfo = finalOrderGrantedDocPack.getDocumentPack(caseData, caseData.getApplicant1());

            letterPrinter.sendLetters(
                caseData,
                caseId,
                caseData.getApplicant1(),
                finalOrderGrantedDocPackInfo,
                finalOrderGrantedDocPack.getLetterId()
            );
        }

        if (caseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent()) {
            DocumentPackInfo conditionalOrderPronouncedDocPackInfo =
                conditionalOrderPronouncedDocPack.getDocumentPack(caseData, caseData.getApplicant1());

            letterPrinter.sendLetters(
                caseData,
                caseId,
                caseData.getApplicant1(),
                conditionalOrderPronouncedDocPackInfo,
                conditionalOrderPronouncedDocPack.getLetterId()
            );
        }
    }

    @Override
    public void sendToApplicant2Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 2 for case : {}", caseId);

        generateAndSendCourtOrdersRegeneratedLetter(caseData, caseId, false);

        if (isNotEmpty(caseData.getConditionalOrder().getCertificateOfEntitlementDocument())) {
            DocumentPackInfo documentPackInfo = certificateOfEntitlementDocPack.getDocumentPack(caseData, caseData.getApplicant2());

            letterPrinter.sendLetters(
                caseData,
                caseId,
                caseData.getApplicant2(),
                documentPackInfo,
                certificateOfEntitlementDocPack.getLetterId()
            );
        }

        if (caseData.getDocuments().getDocumentGeneratedWithType(FINAL_ORDER_GRANTED).isPresent()) {
            DocumentPackInfo finalOrderGrantedDocPackInfo = finalOrderGrantedDocPack.getDocumentPack(caseData, caseData.getApplicant2());

            letterPrinter.sendLetters(
                caseData,
                caseId,
                caseData.getApplicant2(),
                finalOrderGrantedDocPackInfo,
                finalOrderGrantedDocPack.getLetterId()
            );
        }

        if (caseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent()) {
            DocumentPackInfo conditionalOrderPronouncedDocPackInfo =
                conditionalOrderPronouncedDocPack.getDocumentPack(caseData, caseData.getApplicant2());

            letterPrinter.sendLetters(
                caseData,
                caseId,
                caseData.getApplicant2(),
                conditionalOrderPronouncedDocPackInfo,
                conditionalOrderPronouncedDocPack.getLetterId()
            );
        }
    }

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        if (isNotificationRequired(caseData)) {
            log.info("Sending Regenerated Court Orders email notification to applicant 1 for case : {}", caseId);
            sendCitizenNotification(caseData, caseId, WhichApplicant.APPLICANT_1);
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {
        if (isNotificationRequired(caseData)) {
            log.info("Sending Regenerated Court Orders email notification to applicant 2 for case : {}", caseId);
            sendCitizenNotification(caseData, caseId, WhichApplicant.APPLICANT_2);
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        if (isNotificationRequired(caseData)) {
            log.info("Sending Regenerated Court Orders email notification to applicant 1 solicitor for case : {}", caseId);
            sendSolicitorNotification(caseData, caseId, WhichApplicant.APPLICANT_1);
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {
        if (isNotificationRequired(caseData)) {
            log.info("Sending Regenerated Court Orders email notification to applicant 2 solicitor for case : {}", caseId);
            sendSolicitorNotification(caseData, caseId, WhichApplicant.APPLICANT_2);
        }
    }

    private void sendCitizenNotification(final CaseData data, final long caseId, final WhichApplicant whichApplicant) {
        final boolean isApplicant1 = WhichApplicant.APPLICANT_1.equals(whichApplicant);
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final Applicant partner = isApplicant1 ? data.getApplicant2() : data.getApplicant1();

        final Map<String, String> templateVars = commonContent.mainTemplateVars(data, caseId, applicant, partner);
        templateVars.putAll(courtOrderRegeneratedTemplateContent.regenerateTemplateContent(data));

        notificationService.sendEmail(
            applicant.getEmail(),
            COURT_ORDERS_REGENERATED_CITIZEN,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }

    private void sendSolicitorNotification(final CaseData data, final long caseId, final WhichApplicant whichApplicant) {
        final boolean isApplicant1 = WhichApplicant.APPLICANT_1.equals(whichApplicant);
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();

        final Map<String, String> templateVars = commonContent.solicitorTemplateVars(data, caseId, applicant);
        templateVars.putAll(courtOrderRegeneratedTemplateContent.regenerateTemplateContent(data));
        templateVars.put(IS_SOLE, data.getApplicationType().isSole() ? YES : NO);
        templateVars.put(IS_JOINT, !data.getApplicationType().isSole() ? YES : NO);

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            COURT_ORDERS_REGENERATED_SOLICITOR,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }

    private void generateAndSendCourtOrdersRegeneratedLetter(CaseData caseData, Long caseId, boolean isApplicant1) {

        if (isNotificationRequired(caseData)) {
            log.info("Sending court orders regenerated letter to {} for case : {}", isApplicant1 ? "applicant1" : "applicant2", caseId);

            Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();

            Document generatedDocument = generateDocument(caseId, applicant, caseData);

            Letter letter = new Letter(generatedDocument, 1);
            String caseIdString = String.valueOf(caseId);

            final Print print = new Print(
                List.of(letter),
                caseIdString,
                caseIdString,
                LETTER_TYPE_COURT_ORDERS_REGENERATED,
                applicant.getFullName(),
                applicant.getAddressOverseas()
            );

            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        }
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData) {
        return caseDataDocumentService.renderDocument(courtOrderRegeneratedTemplateContent.getTemplateContent(caseData, caseId, applicant),
            caseId,
            CITIZEN_COURT_ORDERS_REGENERATED_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            DOCUMENT_NAME_COURT_ORDERS_REGENERATED);
    }

    private boolean isNotificationRequired(CaseData caseData) {
        return isNotEmpty(caseData.getConditionalOrder().getCertificateOfEntitlementDocument())
            || caseData.getDocuments().getDocumentGeneratedWithType(FINAL_ORDER_GRANTED).isPresent()
            || caseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent();
    }
}
