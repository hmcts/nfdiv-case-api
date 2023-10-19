package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder.CoRefusalDocumentPack;
import uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder.ConditionalOrderRefusedDocumentGenerator;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionalOrderRefusedPrinter {

    private final ConditionalOrderRefusedDocumentGenerator conditionalOrderRefusedDocumentGenerator;
    private final BulkPrintService bulkPrintService;

    public void sendLetters(final CaseData caseData,
                            final Long caseId,
                            final Applicant applicant) {

        CoRefusalDocumentPack documentPackToSend = CoRefusalDocumentPack.refusalPackFromCaseData(caseData, applicant);

        List<Letter> letters = conditionalOrderRefusedDocumentGenerator.generateDocuments(caseData, caseId, applicant, documentPackToSend);

        if (!isEmpty(letters) && letters.size() == documentPackToSend.getDocumentPack().size()) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(
                letters,
                caseIdString,
                caseIdString,
                documentPackToSend.getLetterType().toString(),
                applicant.getFullName()
            );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn("{} Letter pack has missing documents. Expected documents with type {} , for Case ID: {}",
                documentPackToSend.getLetterType().toString(), documentPackToSend.getDocumentPack(), caseId);
        }
    }
}
