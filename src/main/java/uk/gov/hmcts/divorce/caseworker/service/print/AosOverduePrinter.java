package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_OVERDUE_LETTER;

@Component
@Slf4j
public class AosOverduePrinter {

    private static final String LETTER_TYPE_AOS_OVERDUE = "aos-overdue";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendLetterToApplicant(final CaseData caseData, final Long caseId) {

        final List<Letter> letters = lettersWithDocumentType(caseData.getDocumentsGenerated(), AOS_OVERDUE_LETTER);

        if (!isEmpty(letters)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(letters, caseIdString, caseIdString, LETTER_TYPE_AOS_OVERDUE);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AoS Overdue for applicant has missing documents. Expected document with type {} , for Case ID: {}",
                AOS_OVERDUE_LETTER,
                caseId
            );
        }
    }
}
