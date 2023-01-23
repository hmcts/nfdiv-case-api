package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.GenerateJudicialSeparationCORefusedForClarificationCoverLetter;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.legaladvisor.service.task.GenerateCoRefusedCoverLetter;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@Component
@Slf4j
public class AwaitingClarificationApplicationPrinter extends AwaitingAmendedApplicationPrinter {

    @Autowired
    private GenerateCoRefusedCoverLetter generateCoRefusedCoverLetter;

    @Autowired
    private GenerateJudicialSeparationCORefusedForClarificationCoverLetter generateJudicialSeparationCORefusedForClarificationCoverLetter;

    private void logSendLettersWarning(final Boolean isJudicialSeparation, final Long caseId) {
        String defaultMsg =
            "Awaiting clarification Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
        String judicialSeparationMsg =
            "Awaiting clarification JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
        List<DocumentType> docList = new ArrayList<>(
            List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL)
        );
        String message = defaultMsg;
        if (isJudicialSeparation) {
            message = judicialSeparationMsg;
            docList.add(APPLICATION);

        }
        log.warn(message, docList, caseId);
    }

    private String getAwaitingAmendmentOrClarificationLetterType() {
        return awaitingAmendmentOrClarificationLetterType(true);
    }

    private int getExpectedDocumentsSize(final CaseData caseData) {
        return expectedDocumentsSize(caseData.getIsJudicialSeparation().toBoolean(), true);
    }

    private List<Letter> getLetters(final CaseData caseData, final Applicant applicant) {
        return awaitingAmendedOrClarificationApplicationLetters(caseData, applicant, true);
    }

    private DocumentType getRefusalCoverLetterType(final CaseData caseData, final Applicant applicant) {
        return generateJudicialSeparationCORefusedForClarificationCoverLetter.getDocumentType(caseData, applicant);
    }

    private void generateJudicialSeparationCoverLetter(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateJudicialSeparationCORefusedForClarificationCoverLetter.generateAndUpdateCaseData(
            caseData,
            caseId,
            applicant
        );
    }

    private void generateCoverLetter(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateCoRefusedCoverLetter.generateAndUpdateCaseData(
            caseData,
            caseId,
            applicant,
            true
        );
    }
}
