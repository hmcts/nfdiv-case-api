package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_SHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@Component
@Slf4j
public class AwaitingClarificationPrinter {

    private static final String LETTER_TYPE_AWAITING_CLARIFICATION = "awaiting-clarification-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    public void sendLetter(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateLetters(caseData, caseId, applicant);
        final List<Letter> currentAwaitingAmendedApplicationLetters = awaitingAmendedApplicationLetters(caseData);

        if (!isEmpty(currentAwaitingAmendedApplicationLetters)) {

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
                List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_SHEET, CONDITIONAL_ORDER_REFUSAL, APPLICATION),
                caseId
            );
        }
    }

    private List<Letter> awaitingAmendedApplicationLetters(final CaseData caseData) {

        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            COVERSHEET);


        final List<Letter> refusalLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_REFUSAL
        );

        final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            APPLICATION);

        final Letter coversheetLetter = firstElement(coversheetLetters);
        // TODO: add refusal cover sheet
        final Letter refusalLetter = firstElement(refusalLetters);
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);


        final List<Letter> awaitingClarificationLetters = new ArrayList<>();

        if (coversheetLetter != null) {
            awaitingClarificationLetters.add(coversheetLetter);
        }
        if (refusalLetter != null) {
            awaitingClarificationLetters.add(refusalLetter);
        }
        if (null != divorceApplicationLetter) {
            awaitingClarificationLetters.add(divorceApplicationLetter);
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

        // TODO: generate CO REFUSED AMENDED (COVER LETTER)
    }
}
