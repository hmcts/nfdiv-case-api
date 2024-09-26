package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;

@Component
@Slf4j
public class ConditionalOrderPronouncedPrinter {

    public static final String LETTER_TYPE_CO_PRONOUNCED = "conditional-order-pronounced";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendLetter(final CaseData caseData,
                           final Long caseId,
                           final DocumentType coversheetDocumentType,
                           final Applicant applicant) {

        final List<Letter> conditionalOrderPronouncedLetters = conditionalOrderPronouncedLetters(
            caseData,
            coversheetDocumentType
        );

        if (!isEmpty(conditionalOrderPronouncedLetters)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(
                conditionalOrderPronouncedLetters,
                caseIdString,
                caseIdString,
                LETTER_TYPE_CO_PRONOUNCED,
                applicant.getFullName(),
                applicant.getCorrespondenceAddressIsOverseas()
            );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Conditional Order Pronounced print has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(coversheetDocumentType, CONDITIONAL_ORDER_GRANTED),
                caseId);
        }
    }

    private List<Letter> conditionalOrderPronouncedLetters(CaseData caseData, DocumentType coversheetDocumentType) {
        final List<Letter> coversheetLetters = getLettersBasedOnContactPrivacy(caseData, coversheetDocumentType);

        final List<Letter> conditionalOrderGrantedLetters = getLettersBasedOnContactPrivacy(caseData, CONDITIONAL_ORDER_GRANTED);

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
}
