package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REMINDER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;

@Component
@Slf4j
public class ConditionalOrderReminderPrinter {

    @Autowired
    private BulkPrintService bulkPrintService;

    private static final String LETTER_TYPE_CONDITIONAL_ORDER_REMINDER_PACK = "conditional-order-reminder-pack";
    private static final int EXPECTED_DOCUMENTS_SIZE = 3;

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {

        final List<Letter> conditionalOrderLettersToSend = conditionalOrderLetters(caseData);
        if (!isEmpty(conditionalOrderLettersToSend) && conditionalOrderLettersToSend.size() == EXPECTED_DOCUMENTS_SIZE) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(
                conditionalOrderLettersToSend,
                caseIdString,
                caseIdString,
                LETTER_TYPE_CONDITIONAL_ORDER_REMINDER_PACK,
                List.of(caseIdString, applicant.getFullName(), LETTER_TYPE_CONDITIONAL_ORDER_REMINDER_PACK)
            );

            final UUID letterId = bulkPrintService.print(print);
            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn("Conditional order reminder letters missing. Failed to send to bulk print for Case ID: {}", caseId);
        }
    }

    private List<Letter> conditionalOrderLetters(CaseData caseData) {

        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            COVERSHEET);

        final List<Letter> conditionalOrderReminderLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_REMINDER);

        final List<Letter> d84Letters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            D84
        );

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter conditionalOrderReminderLetter = firstElement(conditionalOrderReminderLetters);
        final Letter d84Letter = firstElement(d84Letters);

        final List<Letter> conditionalOrderLetters = new ArrayList<>();

        if (coversheetLetter != null) {
            conditionalOrderLetters.add(coversheetLetter);
        }
        if (conditionalOrderReminderLetter != null) {
            conditionalOrderLetters.add(conditionalOrderReminderLetter);
        }
        if (d84Letter != null) {
            conditionalOrderLetters.add(d84Letter);
        }

        return conditionalOrderLetters;
    }
}
