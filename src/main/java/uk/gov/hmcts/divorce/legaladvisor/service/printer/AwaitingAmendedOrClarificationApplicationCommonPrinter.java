package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderCommonContent;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetSolicitorTemplateContent;
import uk.gov.hmcts.divorce.document.content.GenerateJudicialSeparationCORefusedForAmendmentCoverLetter;
import uk.gov.hmcts.divorce.document.content.GenerateJudicialSeparationCORefusedForClarificationCoverLetter;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.legaladvisor.service.task.GenerateCoRefusedCoverLetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.legaladvisor.service.printer.LetterType.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE;

@Component
@Slf4j
public class AwaitingAmendedOrClarificationApplicationCommonPrinter {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Autowired
    private CoversheetSolicitorTemplateContent coversheetSolicitorTemplateContent;

    @Autowired
    private GenerateCoRefusedCoverLetter generateCoRefusedCoverLetter;

    @Autowired
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    @Autowired
    private GenerateJudicialSeparationCORefusedForAmendmentCoverLetter generateJudicialSeparationCORefusedForAmendmentCoverLetter;

    @Autowired
    private GenerateJudicialSeparationCORefusedForClarificationCoverLetter generateJudicialSeparationCORefusedForClarificationCoverLetter;

    public void sendLetters(
        final CaseData caseData,
        final Long caseId,
        final Applicant applicant,
        final MissingDocumentsValidation missingDocumentsValidation,
        final LetterType awaitingAmendmentOrClarificationLetterType
    ) {
        final boolean isClarificationRefusal = isClarificationRefusal(awaitingAmendmentOrClarificationLetterType);
        generateLetters(caseData, caseId, applicant, isClarificationRefusal);
        final List<Letter> currentLetters = awaitingAmendedOrClarificationApplicationLetters(caseData, applicant, isClarificationRefusal);

        if (!isEmpty(currentLetters)
            && currentLetters.size() == missingDocumentsValidation.expectedDocumentsSize) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(
                currentLetters,
                caseIdString,
                caseIdString,
                awaitingAmendmentOrClarificationLetterType.toString(),
                applicant.getFullName()
            );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(missingDocumentsValidation.message, missingDocumentsValidation.documentTypeList, caseId);
        }
    }

    private Boolean isClarificationRefusal(LetterType awaitingAmendmentOrClarificationLetterType) {
        return awaitingAmendmentOrClarificationLetterType
            .equals(AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE);
    }

    private Letter getFirstLetterWithDocumentType(final CaseData caseData, final DocumentType documentType) {
        final List<Letter> letters = lettersWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), documentType);
        return firstElement(letters);
    }

    private List<Letter> awaitingAmendedOrClarificationApplicationLetters(
        final CaseData caseData,
        final Applicant applicant,
        final Boolean isClarificationRefusal
    ) {

        final Letter coversheetLetter = getFirstLetterWithDocumentType(caseData, COVERSHEET);
        final Letter refusalCoverLetter = getFirstLetterWithDocumentType(
            caseData,
            conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, isClarificationRefusal)
        );
        final Letter refusalLetter = getFirstLetterWithDocumentType(caseData, CONDITIONAL_ORDER_REFUSAL);

        final List<Letter> awaitingAmendmentOrClarificationLetters = new ArrayList<>();

        if (coversheetLetter != null) {
            awaitingAmendmentOrClarificationLetters.add(coversheetLetter);
        }
        if (refusalCoverLetter != null) {
            awaitingAmendmentOrClarificationLetters.add(refusalCoverLetter);
        }
        if (refusalLetter != null) {
            awaitingAmendmentOrClarificationLetters.add(refusalLetter);
        }

        // If not clarification refusal, or if is JS, then add the application to the document list
        if (caseData.isJudicialSeparationCase() || !isClarificationRefusal) {
            final Letter divorceApplicationLetter = getFirstLetterWithDocumentType(caseData, APPLICATION);
            if (divorceApplicationLetter != null) {
                awaitingAmendmentOrClarificationLetters.add(divorceApplicationLetter);
            }
        }

        return awaitingAmendmentOrClarificationLetters;
    }

    private void generateJudicialSeparationLetters(
        final CaseData caseData,
        final Long caseId,
        final Applicant applicant,
        final Boolean isClarificationRefusal
    ) {
        final Map<String, Object> coverSheetTemplateContent = applicant.isRepresented()
            ? coversheetSolicitorTemplateContent.apply(caseId, applicant)
            : coversheetApplicantTemplateContent.apply(caseData, caseId, applicant);

        final String coverSheetTemplateId = applicant.isRepresented()
            ? COVERSHEET_APPLICANT2_SOLICITOR
            : COVERSHEET_APPLICANT;

        generateCoversheet.generateCoversheet(
            caseData,
            caseId,
            coverSheetTemplateId,
            coverSheetTemplateContent,
            applicant.getLanguagePreference()
        );

        if (isClarificationRefusal) {
            generateJudicialSeparationCORefusedForClarificationCoverLetter.generateAndUpdateCaseData(caseData, caseId, applicant);
        } else {
            generateJudicialSeparationCORefusedForAmendmentCoverLetter.generateAndUpdateCaseData(caseData, caseId, applicant);
        }
    }

    private void generateLetters(
        final CaseData caseData,
        final Long caseId,
        final Applicant applicant,
        final Boolean isClarificationRefusal
    ) {
        if (caseData.isJudicialSeparationCase()) {
            generateJudicialSeparationLetters(caseData, caseId, applicant, isClarificationRefusal);
        } else {
            generateCoversheet.generateCoversheet(
                caseData,
                caseId,
                COVERSHEET_APPLICANT,
                coversheetApplicantTemplateContent.apply(caseData, caseId, applicant),
                applicant.getLanguagePreference()
            );

            generateCoRefusedCoverLetter.generateAndUpdateCaseData(
                caseData,
                caseId,
                applicant,
                isClarificationRefusal
            );
        }
    }
}
