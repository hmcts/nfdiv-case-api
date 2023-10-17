package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;

import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.legaladvisor.service.printer.LetterType;

import java.util.Set;

import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER;

public class ClarificationRefusalJsSolicitorPack implements CoRefusalDocumentPack {
    @Override
    public Set<DocumentType> getDocumentPack() {
        return Set.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        );
    }

    @Override
    public String getErrorMessage() {
        return "Awaiting clarification JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
    }

    @Override
    public LetterType getLetterType() {
        return LetterType.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE;
    }

    @Override
    public String getCoverLetterTemplateId() {
        return JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
    }
}
