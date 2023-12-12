package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendAosResponseLetterPackToApplicant implements CaseTask {

    private final AosResponseDocumentPack aosResponseDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final AcknowledgementOfService acknowledgementOfService = caseData.getAcknowledgementOfService();

        if (caseData.getApplicant1().isApplicantOffline()) {

            if (acknowledgementOfService.isDisputed()) {
                log.info("Sending aos response letter (disputed) pack to bulk print as applicant1 is offline. Case id: {}", caseId);
            } else {
                log.info("Sending aos response letter (without dispute) pack to bulk print as applicant1 is offline. Case id: {}", caseId);
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
            log.info("Not sending aos response letter pack to bulk print as applicant1 is not offline. Case id: {}", caseId);
        }
        return caseDetails;
    }
}
