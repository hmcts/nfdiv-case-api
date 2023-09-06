package uk.gov.hmcts.divorce.caseworker.service.print;

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

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;

@Component
@Slf4j
public class FinalOrderGrantedPrinter {

    private static final String LETTER_TYPE_FINAL_ORDER_GRANTED = "final-order-granted-letter";
    private static final int EXPECTED_DOCUMENTS_SIZE = 2;

    @Autowired
    private BulkPrintService bulkPrintService;

    public void print(final CaseData caseData,
                      final Long caseId,
                      final DocumentType coverLetterDocumentType,
                      final Applicant applicant) {

        final List<Letter> finalOrderGrantedLetters = finalOrderGrantedLetters(caseData, coverLetterDocumentType);

        if (!isEmpty(finalOrderGrantedLetters) && finalOrderGrantedLetters.size() == EXPECTED_DOCUMENTS_SIZE) {

            final String caseIdString = caseId.toString();
            final Print print =
                new Print(
                    finalOrderGrantedLetters,
                    caseIdString,
                    caseIdString,
                    LETTER_TYPE_FINAL_ORDER_GRANTED,
                    applicant.getFullName()
                );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Final Order Granted print has missing documents. Expected document with type {} , for Case ID: {}",
                List.of(coverLetterDocumentType, FINAL_ORDER_GRANTED),
                caseId
            );
        }
    }

    private List<Letter> finalOrderGrantedLetters(CaseData caseData, final DocumentType coverLetterDocumentType) {
        final List<Letter> finalOrderGrantedCoverLetters = getLettersBasedOnContactPrivacy(caseData, coverLetterDocumentType);

        final List<Letter> finalOrderGrantedCertificates = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            FINAL_ORDER_GRANTED);

        final Letter finalOrderGrantedCoverLetter = firstElement(finalOrderGrantedCoverLetters);
        final Letter finalOrderGrantedCertificate = firstElement(finalOrderGrantedCertificates);

        final List<Letter> finalOrderGrantedLetters = new ArrayList<>();

        if (null != finalOrderGrantedCoverLetter) {
            finalOrderGrantedLetters.add(finalOrderGrantedCoverLetter);
        }
        if (null != finalOrderGrantedCertificate) {
            finalOrderGrantedLetters.add(finalOrderGrantedCertificate);
        }
        return finalOrderGrantedLetters;
    }
}
