package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;

@RequiredArgsConstructor
@Component
@Slf4j
public class RegenerateConditionalOrderNotification implements ApplicantNotification {
    private final LetterPrinter letterPrinter;
    private final ConditionalOrderPronouncedDocumentPack conditionalOrderPronouncedDocPack;

    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Sending Regenerated Conditional Order to applicant 1 for case : {}", caseId);

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
        log.info("Sending Regenerated Conditional Order to applicant 2 for case : {}", caseId);

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
