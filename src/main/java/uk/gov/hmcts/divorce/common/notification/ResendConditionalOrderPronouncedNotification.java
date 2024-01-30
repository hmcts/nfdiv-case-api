package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResendConditionalOrderPronouncedNotification implements ApplicantNotification {

    private final ConditionalOrderPronouncedDocumentPack conditionalOrderGrantedDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        if (YES.equals(caseData.getApplicant1().getCoPronouncedCoverLetterRegenerated())) {
            log.info("Resending conditional order letter to applicant 1 for case: {}", caseId);
            Applicant applicant1 = caseData.getApplicant1();
            var documentPackInfo = conditionalOrderGrantedDocumentPack.getDocumentPack(caseData, applicant1);
            letterPrinter.sendLetters(caseData, caseId, applicant1, documentPackInfo, conditionalOrderGrantedDocumentPack.getLetterId());
        }
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        if (YES.equals(caseData.getApplicant2().getCoPronouncedCoverLetterRegenerated())) {
            log.info("Resending conditional order letter to applicant 2 for case: {}", caseId);
            Applicant applicant2 = caseData.getApplicant2();
            var documentPackInfo = conditionalOrderGrantedDocumentPack.getDocumentPack(caseData, applicant2);
            letterPrinter.sendLetters(caseData, caseId, applicant2, documentPackInfo, conditionalOrderGrantedDocumentPack.getLetterId());
        }
    }
}
