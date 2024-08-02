package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResendJudicialSeparationCitizenAosResponseNotification implements ApplicantNotification {

    private final AosResponseDocumentPack aosResponseDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        final AcknowledgementOfService acknowledgementOfService = caseData.getAcknowledgementOfService();

        if (YES.equals(caseData.getApplicant1().getJsCitizenAosResponseLetterRegenerated())) {
            if (caseData.getApplicant1().isApplicantOffline()) {
                if (acknowledgementOfService.isDisputed()) {
                    log.info("Resending JS citizen aos response letter (disputed) pack to bulk print as applicant1 is offline. Case id: {}", caseId);
                } else {
                    log.info("Sending JS citizen aos response letter (without dispute) pack to bulk print as applicant1 is offline. Case id: {}", caseId);
                }
                final var documentPack = aosResponseDocumentPack.getDocumentPack(caseData, caseData.getApplicant1());

                letterPrinter.sendLetters(
                    caseData,
                    caseId,
                    caseData.getApplicant1(),
                    documentPack,
                    aosResponseDocumentPack.getLetterId()
                );

            } else {
                log.info("Not resending js citizen aos response letter pack to bulk print as applicant1 is not offline. Case id: {}", caseId);
            }
        } else {
            log.info("Not resending js citizen aos response letter pack to bulk print as letter not regenerated. Case id: {}", caseId);
        }
    }
}
