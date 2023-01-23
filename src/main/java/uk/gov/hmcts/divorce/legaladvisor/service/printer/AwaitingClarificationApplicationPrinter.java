package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER;

@Component
@Slf4j
public class AwaitingClarificationApplicationPrinter {

    @Autowired
    private AwaitingAmendedOrClarificationApplicationCommonPrinter awaitingAmendedOrClarificationApplicationCommonPrinter;

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        final String clarificationMessage =
            "Awaiting clarification Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
        final String judicialSeparationClarificationMessage =
            "Awaiting clarification JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
        MissingDocumentsValidation missingDocumentsValidation = new MissingDocumentsValidation(
            clarificationMessage,
            List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL),
            3
        );
        if (caseData.getIsJudicialSeparation().toBoolean()) {
            missingDocumentsValidation.message = judicialSeparationClarificationMessage;
            missingDocumentsValidation.documentTypeList =
                applicant.isRepresented()
                    ? List.of(
                        COVERSHEET,
                        JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
                        CONDITIONAL_ORDER_REFUSAL,
                        APPLICATION
                    )
                    : List.of(
                        COVERSHEET,
                        CONDITIONAL_ORDER_REFUSAL_COVER_LETTER,
                        // Check this - which doc should be sent to unrepresented applicants for JS clarification refusal?
                        CONDITIONAL_ORDER_REFUSAL,
                        APPLICATION
                    );
            missingDocumentsValidation.expectedDocumentsSize = 4;
        }

        awaitingAmendedOrClarificationApplicationCommonPrinter.sendLetters(
            caseData,
            caseId,
            applicant,
            missingDocumentsValidation,
            true
        );
    }
}
