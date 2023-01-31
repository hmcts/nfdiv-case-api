package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.GenerateJudicialSeparationCORefusedForAmendmentCoverLetter;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.legaladvisor.service.task.GenerateCoRefusedCoverLetter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;

@Component
@Slf4j
public class AwaitingAmendedApplicationPrinter {

    private static final String LETTER_TYPE_AWAITING_CLARIFICATION = "awaiting-amended-application-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Autowired
    private GenerateCoRefusedCoverLetter generateCoRefusedCoverLetter;

    @Autowired
    private GenerateJudicialSeparationCORefusedForAmendmentCoverLetter generateJudicialSeparationCORefusedForAmendmentCoverLetter;

    private static final int EXPECTED_DOCUMENTS_SIZE = 4;

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateLetters(caseData, caseId, applicant);
        final List<Letter> currentAwaitingAmendedApplicationLetters = awaitingAmendedApplicationLetters(caseData);

        if (!isEmpty(currentAwaitingAmendedApplicationLetters)
            && currentAwaitingAmendedApplicationLetters.size() == EXPECTED_DOCUMENTS_SIZE) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(
                currentAwaitingAmendedApplicationLetters,
                caseIdString,
                caseIdString,
                LETTER_TYPE_AWAITING_CLARIFICATION
            );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Awaiting Amended Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL, APPLICATION),
                caseId
            );
        }
    }

    private List<Letter> awaitingAmendedApplicationLetters(final CaseData caseData) {

        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            COVERSHEET);

        DocumentType refusalCoverLetterType = caseData.isJudicialSeparationCase()
            ? JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER
            : CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;

        final List<Letter> refusalCoverLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            refusalCoverLetterType
        );

        final List<Letter> refusalLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_REFUSAL
        );

        final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            APPLICATION
        );

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter refusalCoverLetter = firstElement(refusalCoverLetters);
        final Letter refusalLetter = firstElement(refusalLetters);
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);

        final List<Letter> awaitingAmendmentLetters = new ArrayList<>();

        if (coversheetLetter != null) {
            awaitingAmendmentLetters.add(coversheetLetter);
        }
        if (refusalCoverLetter != null) {
            awaitingAmendmentLetters.add(refusalCoverLetter);
        }
        if (refusalLetter != null) {
            awaitingAmendmentLetters.add(refusalLetter);
        }
        if (null != divorceApplicationLetter) {
            awaitingAmendmentLetters.add(divorceApplicationLetter);
        }

        return awaitingAmendmentLetters;
    }

    private void generateLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateCoversheet.generateCoversheet(
            caseData,
            caseId,
            COVERSHEET_APPLICANT,
            coversheetApplicantTemplateContent.apply(caseData, caseId, applicant),
            applicant.getLanguagePreference()
        );
        if (caseData.isJudicialSeparationCase()) {
            generateJudicialSeparationCORefusedForAmendmentCoverLetter.generateAndUpdateCaseData(
                caseData,
                caseId,
                JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID,
                applicant
            );
        } else {
            generateCoRefusedCoverLetter.generateAndUpdateCaseData(
                caseData,
                caseId,
                REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID,
                applicant
            );
        }
    }
}
