package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithConfidentialDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;

@Component
@Slf4j
public class ConditionalOrderPronouncedPrinter {

    public static final String LETTER_TYPE_CO_PRONOUNCED = "conditional-order-pronounced";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendLetter(final CaseData caseData, final Long caseId, final DocumentType coversheetDocumentType) {

        final List<Letter> conditionalOrderPronouncedLetters = conditionalOrderPronouncedLetters(caseData, coversheetDocumentType);

        if (!isEmpty(conditionalOrderPronouncedLetters)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(conditionalOrderPronouncedLetters, caseIdString, caseIdString, LETTER_TYPE_CO_PRONOUNCED);
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

        boolean isContactPrivate;

        if (CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1.equals(coversheetDocumentType)) {
            isContactPrivate = caseData.getApplicant1().isConfidentialContactDetails();
        } else {
            isContactPrivate = caseData.getApplicant2().isConfidentialContactDetails();
        }

        CaseDocuments caseDocuments = caseData.getDocuments();

        final List<Letter> coversheetLetters = isContactPrivate
            ? lettersWithConfidentialDocumentType(
                caseDocuments.getConfidentialDocumentsGenerated(),
                getConfidentialDocumentType(coversheetDocumentType))
            : lettersWithDocumentType(caseDocuments.getDocumentsGenerated(), coversheetDocumentType);

        final List<Letter> conditionalOrderGrantedLetters = lettersWithDocumentType(
            caseDocuments.getDocumentsGenerated(),
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
}
