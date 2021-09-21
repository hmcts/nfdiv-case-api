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

import static java.util.Arrays.asList;
import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_INVITATION;


@Component
@Slf4j
public class AosPackPrinter {

    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";
    private static final List<DocumentType> AOS_DOCUMENT_TYPES = asList(
        APPLICATION,
        RESPONDENT_INVITATION);

    @Autowired
    private BulkPrintService bulkPrintService;

    public void print(final CaseData caseData, final Long caseId) {

        final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
            caseData.getDocumentsGenerated(),
            APPLICATION);

        final List<Letter> respondentInvitationLetters = lettersWithDocumentType(
            caseData.getDocumentsGenerated(),
            RESPONDENT_INVITATION);

        //Always get document on top of list as new document is added to top after generation
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);

        //Always get document on top of list as new document is added to top after generation
        final Letter respondentInvitationLetter = firstElement(respondentInvitationLetters);

        final List<Letter> currentAosLetters = new ArrayList<>();

        if (null != divorceApplicationLetter) {
            currentAosLetters.add(divorceApplicationLetter);
        }
        if (null != respondentInvitationLetter) {
            currentAosLetters.add(respondentInvitationLetter);
        }

        if (!isEmpty(currentAosLetters)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(currentAosLetters, caseIdString, caseIdString, LETTER_TYPE_RESPONDENT_PACK);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AoS Pack print has missing documents. Expected documents with type {} , for Case ID: {}",
                AOS_DOCUMENT_TYPES,
                caseId);
        }
    }
}
