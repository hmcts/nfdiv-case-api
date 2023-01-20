package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.GenerateJudicialSeparationCORefusedForClarificationCoverLetter;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.legaladvisor.service.task.GenerateCoRefusedCoverLetter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@Component
@Slf4j
public class AwaitingClarificationApplicationPrinter {

    private static final String LETTER_TYPE_AWAITING_CLARIFICATION = "awaiting-clarification-application-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Autowired
    private GenerateCoRefusedCoverLetter generateCoRefusedCoverLetter;

    @Autowired
    private GenerateJudicialSeparationCORefusedForClarificationCoverLetter generateJudicialSeparationCORefusedForClarificationCoverLetter;

    private static final int EXPECTED_DOCUMENTS_SIZE = 3;

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateLetters(caseData, caseId, applicant);
        final List<Letter> currentAwaitingClarificationApplicationLetters = awaitingClarificationApplicationLetters(caseData, applicant);

        if (!isEmpty(currentAwaitingClarificationApplicationLetters)
            && currentAwaitingClarificationApplicationLetters.size() == EXPECTED_DOCUMENTS_SIZE) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(
                currentAwaitingClarificationApplicationLetters,
                caseIdString,
                caseIdString,
                LETTER_TYPE_AWAITING_CLARIFICATION
            );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Awaiting clarification Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL),
                caseId
            );
        }
    }

    private List<Letter> awaitingClarificationApplicationLetters(final CaseData caseData, final Applicant applicant) {

        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            COVERSHEET
        );

        final List<Letter> refusalCoverLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            generateJudicialSeparationCORefusedForClarificationCoverLetter.getDocumentType(caseData, applicant)
        );

        final List<Letter> refusalLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_REFUSAL
        );

        final List<Letter> awaitingClarificationLetters = new ArrayList<>();

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter refusalCoverLetter = firstElement(refusalCoverLetters);
        final Letter refusalLetter = firstElement(refusalLetters);

        if (coversheetLetter != null) {
            awaitingClarificationLetters.add(coversheetLetter);
        }
        if (refusalCoverLetter != null) {
            awaitingClarificationLetters.add(refusalCoverLetter);
        }
        if (refusalLetter != null) {
            awaitingClarificationLetters.add(refusalLetter);
        }
        if (caseData.getIsJudicialSeparation().toBoolean()) {
            final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
                caseData.getDocuments().getDocumentsGenerated(),
                APPLICATION
            );
            final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);
            if (divorceApplicationLetter != null) {
                awaitingClarificationLetters.add(divorceApplicationLetter);
            }
        }

        return awaitingClarificationLetters;
    }

    private void generateLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateCoversheet.generateCoversheet(
            caseData,
            caseId,
            COVERSHEET_APPLICANT,
            coversheetApplicantTemplateContent.apply(caseData, caseId, applicant),
            applicant.getLanguagePreference()
        );
        if (caseData.getApplication().isPaperCase() && applicant.isRepresented() && caseData.getIsJudicialSeparation().toBoolean()) {
            generateJudicialSeparationCORefusedForClarificationCoverLetter.generateAndUpdateCaseData(
                caseData,
                caseId,
                applicant
            );
        } else {
            generateCoRefusedCoverLetter.generateAndUpdateCaseData(
                caseData,
                caseId,
                applicant,
                true
            );
        }
    }
}
