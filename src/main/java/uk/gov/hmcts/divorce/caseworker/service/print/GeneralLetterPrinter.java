package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;

@Component
@Slf4j
public class GeneralLetterPrinter {

    private static final String LETTER_TYPE_GENERAL_LETTER = "general-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendLetterWithAttachments(final CaseData caseData, final Long caseId) {

        final List<Letter> generalLetters = lettersWithDocumentType(caseData.getDocumentsGenerated(), GENERAL_LETTER);
        final List<Letter> attachments = lettersWithDocumentType(caseData.getGeneralLetter().getGeneralLetterAttachments());

        Letter generalLetter = firstElement(generalLetters);

        if (generalLetter != null) {

            final List<Letter> lettersToPrint = new ArrayList<>();

            lettersToPrint.add(generalLetter);

            if (!CollectionUtils.isEmpty(attachments)) {
                lettersToPrint.addAll(attachments);
            }

            final String caseIdString = caseId.toString();
            final Print print = new Print(lettersToPrint, caseIdString, caseIdString, LETTER_TYPE_GENERAL_LETTER);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "General letter has missing documents. Expected document with type {} , for Case ID: {}",
                GENERAL_LETTER,
                caseId
            );
        }
    }
}
