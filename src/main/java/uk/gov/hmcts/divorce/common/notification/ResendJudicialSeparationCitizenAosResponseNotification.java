package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResendJudicialSeparationCitizenAosResponseNotification implements ApplicantNotification {

    private final AosResponseDocumentPack aosResponseDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        final AcknowledgementOfService acknowledgementOfService = caseData.getAcknowledgementOfService();

        log.info("Resending JS citizen aos response ({}) pack to bulk print. Case id: {}",
            acknowledgementOfService.isDisputed() ? "disputed" : "without dispute",
            caseId
        );

        final var documentPack = aosResponseDocumentPack.getDocumentPack(caseData, caseData.getApplicant1());

        letterPrinter.sendLetters(
            caseData,
            caseId,
            caseData.getApplicant1(),
            documentPack,
            aosResponseDocumentPack.getLetterId()
        );
    }
}
