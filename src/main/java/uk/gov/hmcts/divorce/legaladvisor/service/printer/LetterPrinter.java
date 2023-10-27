package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder.DocumentGenerator;
import uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder.DocumentPackInfo;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public class LetterPrinter {

    private final DocumentGenerator documentGenerator;
    private final BulkPrintService bulkPrintService;

    public void sendLetters(final CaseData caseData,
                            final Long caseId,
                            final Applicant applicant,
                            final DocumentPackInfo documentPackInfo,
                            final String letterName) {

        List<Letter> letters = documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo);

        if (!isEmpty(letters) && letters.size() == documentPackInfo.getDocumentPack().size()) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(
                letters,
                caseIdString,
                caseIdString,
                letterName,
                applicant.getFullName()
            );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn("{} Letter pack has missing documents. Expected documents with type {} , for Case ID: {}",
                letterName, documentPackInfo.getDocumentPack().keySet(), caseId);
        }
    }
}
