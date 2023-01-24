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
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.legaladvisor.service.printer.LetterType.AWAITING_AMENDED_APPLICATION_LETTER_TYPE;

@Component
@Slf4j
public class AwaitingAmendedApplicationPrinter {

    @Autowired
    private AwaitingAmendedOrClarificationApplicationCommonPrinter awaitingAmendedOrClarificationApplicationCommonPrinter;

    public static final String missingDocumentsMessage =
        "Awaiting Amended Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
    public static final String missingDocumentsJudicialSeparationMessage =
        "Awaiting Amended JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
    public static final List<DocumentType> missingDocumentsTypeList =
        List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL, APPLICATION);
    public static final List<DocumentType> missingDocumentsJudicialSeparationTypeList =
        List.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        );
    public static final List<DocumentType> missingDocumentsJudicialSeparationRepresentedTypeList =
        List.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        );
    public static final int missingDocumentsExpectedDocumentsSize = 4;

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        MissingDocumentsValidation missingDocumentsValidation = MissingDocumentsValidation.builder()
            .message(missingDocumentsMessage)
            .documentTypeList(missingDocumentsTypeList)
            .expectedDocumentsSize(missingDocumentsExpectedDocumentsSize)
            .build();

        if (caseData.getIsJudicialSeparation().toBoolean()) {
            missingDocumentsValidation.message = missingDocumentsJudicialSeparationMessage;
            missingDocumentsValidation.documentTypeList = applicant.isRepresented()
                ? missingDocumentsJudicialSeparationRepresentedTypeList
                : missingDocumentsJudicialSeparationTypeList;
        }

        awaitingAmendedOrClarificationApplicationCommonPrinter.sendLetters(
            caseData,
            caseId,
            applicant,
            missingDocumentsValidation,
            AWAITING_AMENDED_APPLICATION_LETTER_TYPE
        );
    }
}
