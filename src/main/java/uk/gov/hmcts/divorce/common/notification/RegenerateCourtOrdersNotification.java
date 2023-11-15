package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.CertificateOfEntitlementDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

@RequiredArgsConstructor
@Component
@Slf4j
public class RegenerateCourtOrdersNotification implements ApplicantNotification {
    private final CertificateOfEntitlementDocumentPack certificateOfEntitlementDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 1 for case : {}", caseId);
        sendLetterWithDocumentPack(caseData, caseData.getApplicant1(), caseId);
    }

    @Override
    public void sendToApplicant2Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Court Orders to applicant 2 for case : {}", caseId);
        sendLetterWithDocumentPack(caseData, caseData.getApplicant2(), caseId);
    }

    private void sendLetterWithDocumentPack(CaseData caseData, Applicant applicant, long caseId) {
        var documentPackInfo = certificateOfEntitlementDocumentPack.getDocumentPack(caseData, applicant);
        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, certificateOfEntitlementDocumentPack.getLetterId());
    }
}
