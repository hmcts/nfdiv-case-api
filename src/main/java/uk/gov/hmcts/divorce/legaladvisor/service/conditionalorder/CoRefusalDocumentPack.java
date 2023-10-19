package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.legaladvisor.service.printer.LetterType;

import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER;

@RequiredArgsConstructor
@Getter
public enum CoRefusalDocumentPack {

    AMENDMENT_JS_PACK(
        Set.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        ),
        LetterType.AWAITING_AMENDED_APPLICATION_LETTER_TYPE,
        JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID
    ),
    AMENDMENT_JS_SOL_PACK(
        Set.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        ),
        LetterType.AWAITING_AMENDED_APPLICATION_LETTER_TYPE,
        JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID
    ),
    AMENDMENT_PACK(
        Set.of(
            COVERSHEET,
            CONDITIONAL_ORDER_REFUSAL_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        ),
        LetterType.AWAITING_AMENDED_APPLICATION_LETTER_TYPE,
        REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID
    ),
    AMENDMENT_SEPARATION_PACK(
        Set.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        ),
        LetterType.AWAITING_AMENDED_APPLICATION_LETTER_TYPE,
        JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID
    ),
    AMENDMENT_SOL_SEPARATION_PACK(
        Set.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        ),
        LetterType.AWAITING_AMENDED_APPLICATION_LETTER_TYPE,
        JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID
    ),
    CLARIFICATION_JS_PACK(
        Set.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        ),
        LetterType.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE,
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID
    ),
    CLARIFICATION_SOL_JS_PACK(
        Set.of(
            COVERSHEET,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL,
            APPLICATION
        ),
        LetterType.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE,
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID
    ),
    CLARIFICATION_PACK(
        Set.of(
            COVERSHEET,
            CONDITIONAL_ORDER_REFUSAL_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL
        ),
        LetterType.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE,
        CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID
    ),
    CLARIFICATION_SEPARATION_PACK(
        Set.of(
            COVERSHEET,
            SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL
        ),
        LetterType.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE,
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID
    ),
    CLARIFICATION_SOL_SEPARATION_PACK(
        Set.of(
            COVERSHEET,
            SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
            CONDITIONAL_ORDER_REFUSAL
        ),
        LetterType.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE,
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID
    );

    private final Set<DocumentType> documentPack;
    private final LetterType letterType;
    private final String coverLetterTemplateId;

    public static CoRefusalDocumentPack refusalPackFromCaseData(CaseData caseData,
                                                                Applicant applicant) {
        if (caseData.isJudicialSeparationCase()) {
            if (MORE_INFO.equals(caseData.getConditionalOrder().getRefusalDecision())) {
                if (applicant.isRepresented()) {
                    return caseData.isDivorce() ? CLARIFICATION_SOL_JS_PACK : CLARIFICATION_SOL_SEPARATION_PACK;
                }
                return caseData.isDivorce() ? CLARIFICATION_JS_PACK : CLARIFICATION_SEPARATION_PACK;
            } else {
                if (applicant.isRepresented()) {
                    return caseData.isDivorce() ? AMENDMENT_JS_SOL_PACK : AMENDMENT_SOL_SEPARATION_PACK;
                }
                return caseData.isDivorce() ? AMENDMENT_JS_PACK : AMENDMENT_SEPARATION_PACK;
            }
        }

        return MORE_INFO.equals(caseData.getConditionalOrder().getRefusalDecision())
            ? CLARIFICATION_PACK : AMENDMENT_PACK;
    }
}
