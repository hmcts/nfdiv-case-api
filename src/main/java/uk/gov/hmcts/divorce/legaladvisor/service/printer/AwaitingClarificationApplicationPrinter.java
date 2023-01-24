package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

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

    public final String AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE = "awaiting-clarification-application-letter";
    public final String MISSING_DOCUMENTS_MESSAGE =
        "Awaiting clarification Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
    public final String MISSING_DOCUMENTS_JUDICIAL_SEPARATION_MESSAGE =
        "Awaiting clarification JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
    public final List<DocumentType> MISSING_DOCUMENTS_TYPE_LIST =
        List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL);
    public final List<DocumentType> MISSING_DOCUMENTS_JUDICIAL_SEPARATION_TYPE_LIST =
        List.of(
            COVERSHEET,
            CONDITIONAL_ORDER_REFUSAL_COVER_LETTER,
            // Check this - which doc should be sent to unrepresented applicants for JS clarification refusal?
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        );
    public final List<DocumentType> MISSING_DOCUMENTS_JUDICIAL_SEPARATION_REPRESENTED_TYPE_LIST =
        List.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        );
    public final int MISSING_DOCUMENTS_EXPECTED_DOCUMENTS_SIZE = 3;
    public final int MISSING_DOCUMENTS_JUDICIAL_SEPARATION_EXPECTED_DOCUMENTS_SIZE = 4;

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        MissingDocumentsValidation missingDocumentsValidation = MissingDocumentsValidation.builder()
            .message(MISSING_DOCUMENTS_MESSAGE)
            .documentTypeList(MISSING_DOCUMENTS_TYPE_LIST)
            .expectedDocumentsSize(MISSING_DOCUMENTS_EXPECTED_DOCUMENTS_SIZE)
            .build();

        if (caseData.getIsJudicialSeparation().toBoolean()) {
            missingDocumentsValidation.message = MISSING_DOCUMENTS_JUDICIAL_SEPARATION_MESSAGE;
            missingDocumentsValidation.documentTypeList = applicant.isRepresented()
                ? MISSING_DOCUMENTS_JUDICIAL_SEPARATION_REPRESENTED_TYPE_LIST
                : MISSING_DOCUMENTS_JUDICIAL_SEPARATION_TYPE_LIST;
            missingDocumentsValidation.expectedDocumentsSize = MISSING_DOCUMENTS_JUDICIAL_SEPARATION_EXPECTED_DOCUMENTS_SIZE;
        }

        awaitingAmendedOrClarificationApplicationCommonPrinter.sendLetters(
            caseData,
            caseId,
            applicant,
            missingDocumentsValidation,
            AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE
        );
    }
}
