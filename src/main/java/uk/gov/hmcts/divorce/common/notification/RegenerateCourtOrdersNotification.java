package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.CertificateOfEntitlementDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.documentpack.FinalOrderGrantedDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;

@RequiredArgsConstructor
@Component
@Slf4j
public class RegenerateCourtOrdersNotification implements ApplicantNotification {
    private final LetterPrinter letterPrinter;
    private final CertificateOfEntitlementDocumentPack certificateOfEntitlementDocPack;
    private final FinalOrderGrantedDocumentPack finalOrderGrantedDocPack;
    private final ConditionalOrderPronouncedDocumentPack conditionalOrderPronouncedDocPack;


    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 1 for case : {}", caseId);

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
}
