package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithAosScannedDocument;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_INVITATION;


@Component
@Slf4j
public class AosPackPrinter {

    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";
    private static final String LETTER_TYPE_APPLICANT_PACK = "applicant-aos-pack";
    private static final String LETTER_TYPE_AOS_RESPONSE_PACK = "aos-response-pack";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendAosLetterToRespondent(final CaseData caseData, final Long caseId) {

        final List<Letter> currentAosLetters = aosLetters(caseData, RESPONDENT_INVITATION);

        if (!isEmpty(currentAosLetters)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(currentAosLetters, caseIdString, caseIdString, LETTER_TYPE_RESPONDENT_PACK);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AoS Pack print for respondent has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, RESPONDENT_INVITATION),
                caseId);
        }
    }

    public void sendAosLetterToApplicant(final CaseData caseData, final Long caseId) {

        final List<Letter> currentAosLetters = aosLetters(caseData, NOTICE_OF_PROCEEDINGS);

        if (!isEmpty(currentAosLetters)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(currentAosLetters, caseIdString, caseIdString, LETTER_TYPE_APPLICANT_PACK);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AoS Pack for print applicant has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS),
                caseId
            );
        }
    }

    public void sendAosResponseLetterToApplicant(final CaseData caseData, final Long caseId) {

        final List<Letter> aosResponseLetters = lettersWithDocumentType(
            caseData.getDocumentsGenerated(),
            AOS_RESPONSE_LETTER);

        final List<Letter> aosLetters = lettersWithAosScannedDocument(caseData.getScannedDocuments());

        final Letter aosResponseLetter = firstElement(aosResponseLetters);

        final Letter aosLetter = firstElement(aosLetters);

        final List<Letter> aosResponseLetterWithAos = new ArrayList<>();

        if (null != aosResponseLetter) {
            aosResponseLetterWithAos.add(aosResponseLetter);
        }
        if (null != aosLetter) {
            aosResponseLetterWithAos.add(aosLetter);
        }

        if (!isEmpty(aosResponseLetterWithAos)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(aosResponseLetterWithAos, caseIdString, caseIdString, LETTER_TYPE_AOS_RESPONSE_PACK);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Aos response letter for applicant has missing documents. Expected documents with type {} , for case id: {}",
                List.of(AOS_RESPONSE_LETTER, ACKNOWLEDGEMENT_OF_SERVICE),
                caseId
            );
        }
    }

    private List<Letter> aosLetters(CaseData caseData, DocumentType documentType) {
        final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
            caseData.getDocumentsGenerated(),
            APPLICATION);

        final List<Letter> notificationLetters = lettersWithDocumentType(
            caseData.getDocumentsGenerated(),
            documentType);

        //Always get document on top of list as new document is added to top after generation
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);

        //Always get document on top of list as new document is added to top after generation
        final Letter notificationLetter = firstElement(notificationLetters);

        final List<Letter> currentAosLetters = new ArrayList<>();

        if (null != notificationLetter) {
            currentAosLetters.add(notificationLetter);
        }
        if (null != divorceApplicationLetter) {
            currentAosLetters.add(divorceApplicationLetter);
        }
        return currentAosLetters;
    }
}
