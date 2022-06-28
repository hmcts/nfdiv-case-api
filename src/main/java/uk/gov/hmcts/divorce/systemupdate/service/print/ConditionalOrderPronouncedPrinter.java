package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.ORDER_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.ORDER_COVERSHEET;

@Component
@Slf4j
public class ConditionalOrderPronouncedPrinter {

    private static final String LETTER_TYPE_CO_PRONOUNCED = "conditional-order-pronounced";

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    public void sendLetter(final CaseData caseData, final Long caseId) {

        final List<Letter> conditionalOrderPronouncedLetters = conditionalOrderPronouncedLetters(caseData);

        if (!isEmpty(conditionalOrderPronouncedLetters)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(conditionalOrderPronouncedLetters, caseIdString, caseIdString, LETTER_TYPE_CO_PRONOUNCED);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Conditional Order Pronounced print has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(ORDER_COVERSHEET, CONDITIONAL_ORDER_GRANTED),
                caseId);
        }
    }

    private List<Letter> conditionalOrderPronouncedLetters(CaseData caseData) {
        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            ORDER_COVERSHEET);

        final List<Letter> conditionalOrderGrantedLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_GRANTED);

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter conditionalOrderGrantedLetter = firstElement(conditionalOrderGrantedLetters);

        final List<Letter> currentConditionalOrderPronouncedLetters = new ArrayList<>();

        if (null != coversheetLetter) {
            currentConditionalOrderPronouncedLetters.add(coversheetLetter);
        }
        if (null != conditionalOrderGrantedLetter) {
            currentConditionalOrderPronouncedLetters.add(conditionalOrderGrantedLetter);
        }
        return currentConditionalOrderPronouncedLetters;
    }

    private void generateCoversheet(final CaseData caseData,
                                    final Long caseId,
                                    final Applicant applicant) {
        final Map<String, Object> templateContent = new HashMap<>();

        log.info("Generating order coversheet for sole case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            ORDER_COVERSHEET,
            templateContent,
            caseId,
            templateId,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, ORDER_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }
}
