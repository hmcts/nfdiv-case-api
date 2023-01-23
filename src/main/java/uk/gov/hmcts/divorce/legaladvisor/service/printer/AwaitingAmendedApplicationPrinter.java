package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetSolicitorTemplateContent;
import uk.gov.hmcts.divorce.document.content.GenerateJudicialSeparationCORefusedForAmendmentCoverLetter;
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
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@Component
@Slf4j
public class AwaitingAmendedApplicationPrinter {

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
    private GenerateJudicialSeparationCORefusedForAmendmentCoverLetter generateJudicialSeparationCORefusedForAmendmentCoverLetter;

    void logSendLettersWarning(final Boolean isJudicialSeparation, final Long caseId) {
        String defaultMsg =
            "Awaiting Amended Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
        String judicialSeparationMsg =
            "Awaiting Amended JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
        String message = isJudicialSeparation ? judicialSeparationMsg : defaultMsg;
        log.warn(
            message,
            List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL, APPLICATION),
            caseId
        );
    }

    String awaitingAmendmentOrClarificationLetterType(final Boolean isClarificationRefusal) {
        return isClarificationRefusal
            ? "awaiting-clarification-application-letter"
            : "awaiting-amended-application-letter";
    }

    String getAwaitingAmendmentOrClarificationLetterType() {
        return awaitingAmendmentOrClarificationLetterType(false);
    }

    int expectedDocumentsSize(final Boolean isJudicialSeparation, final Boolean isClarificationRefusal) {
        if (isJudicialSeparation || !isClarificationRefusal) {
            return 4;
        }
        return 3;
    }

    int getExpectedDocumentsSize(final CaseData caseData) {
        return expectedDocumentsSize(caseData.getIsJudicialSeparation().toBoolean(), false);
    }

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateLetters(caseData, caseId, applicant);
        final List<Letter> currentLetters = getLetters(caseData, applicant);

        if (!isEmpty(currentLetters)
            && currentLetters.size() == getExpectedDocumentsSize(caseData)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(
                currentLetters,
                caseIdString,
                caseIdString,
                getAwaitingAmendmentOrClarificationLetterType()
            );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            logSendLettersWarning(caseData.getIsJudicialSeparation().toBoolean(), caseId);
        }
    }

    List<Letter> getLetters(final CaseData caseData, final Applicant applicant) {
        return awaitingAmendedOrClarificationApplicationLetters(caseData, applicant, false);
    }

    DocumentType getRefusalCoverLetterType(final CaseData caseData, final Applicant applicant) {
        return generateJudicialSeparationCORefusedForAmendmentCoverLetter.getDocumentType(caseData, applicant);
    }

    private Letter getFirstLetterWithDocumentType(final CaseData caseData, final DocumentType documentType) {
        final List<Letter> letters = lettersWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), documentType);
        return firstElement(letters);
    }

    List<Letter> awaitingAmendedOrClarificationApplicationLetters(
        final CaseData caseData,
        final Applicant applicant,
        final Boolean isClarificationRefusal) {

        final Letter coversheetLetter = getFirstLetterWithDocumentType(caseData, COVERSHEET);
        final Letter refusalCoverLetter = getFirstLetterWithDocumentType(caseData, getRefusalCoverLetterType(caseData, applicant));
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
        if (caseData.getIsJudicialSeparation().toBoolean() || !isClarificationRefusal) {
            final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
                caseData.getDocuments().getDocumentsGenerated(),
                APPLICATION
            );
            final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);
            if (divorceApplicationLetter != null) {
                awaitingAmendmentOrClarificationLetters.add(divorceApplicationLetter);
            }
        }

        return awaitingAmendmentOrClarificationLetters;
    }

    void generateJudicialSeparationCoverLetter(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateJudicialSeparationCORefusedForAmendmentCoverLetter.generateAndUpdateCaseData(
            caseData,
            caseId,
            applicant
        );
    }

    void generateCoverLetter(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateCoRefusedCoverLetter.generateAndUpdateCaseData(
            caseData,
            caseId,
            applicant
        );
    }

    private void generateLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        if (caseData.getIsJudicialSeparation().toBoolean()) {
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

            generateJudicialSeparationCoverLetter(caseData, caseId, applicant);
        } else {
            generateCoversheet.generateCoversheet(
                caseData,
                caseId,
                COVERSHEET_APPLICANT,
                coversheetApplicantTemplateContent.apply(caseData, caseId, applicant),
                applicant.getLanguagePreference()
            );

            generateCoverLetter(caseData, caseId, applicant);
        }
    }
}
