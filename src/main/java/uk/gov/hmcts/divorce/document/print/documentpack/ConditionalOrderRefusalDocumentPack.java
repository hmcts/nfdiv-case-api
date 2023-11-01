package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
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

@Component
@RequiredArgsConstructor
public class ConditionalOrderRefusalDocumentPack implements DocumentPack {

    private static final String CONDITIONAL_ORDER_REFUSED_LETTER = "conditional-order-refused";

    private static final DocumentPackInfo AMENDMENT_JS_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER, Optional.of(JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
    ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME));

    private static final DocumentPackInfo AMENDMENT_JS_SOL_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
        JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER, Optional.of(JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
        APPLICATION, Optional.empty()
    ), ImmutableMap.of(
        COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
        JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
    ));

    private static final DocumentPackInfo AMENDMENT_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
        CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, Optional.of(REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
        APPLICATION, Optional.empty()
    ), ImmutableMap.of(
        COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
        REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
    ));

    private static final DocumentPackInfo AMENDMENT_SEPARATION_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
        JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER, Optional.of(JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
        APPLICATION, Optional.empty()
    ), ImmutableMap.of(
        COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
        JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
    ));

    private static final DocumentPackInfo AMENDMENT_SOL_SEPARATION_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
        JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER,
        Optional.of(JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
        APPLICATION, Optional.empty()
    ), ImmutableMap.of(
        COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
        JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
    ));

    private static final DocumentPackInfo CLARIFICATION_JS_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
        Optional.of(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
        APPLICATION, Optional.empty()
    ), ImmutableMap.of(
        COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
    ));

    private static final DocumentPackInfo CLARIFICATION_SEPARATION_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
        SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
        Optional.of(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
        APPLICATION, Optional.empty()
    ), ImmutableMap.of(
        COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
    ));

    private static final DocumentPackInfo CLARIFICATION_SOL_SEPARATION_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
        SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
        Optional.of(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
        APPLICATION, Optional.empty()
    ), ImmutableMap.of(
        COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
        REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
    ));

    private static final DocumentPackInfo CLARIFICATION_SOL_JS_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
        Optional.of(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
        APPLICATION, Optional.empty()
    ), ImmutableMap.of(
        COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
        JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
        REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
    ));

    private static final DocumentPackInfo CLARIFICATION_PACK = new DocumentPackInfo(ImmutableMap.of(
        COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
        CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, Optional.of(CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID),
        CONDITIONAL_ORDER_REFUSAL, Optional.empty()),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData,
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

    @Override
    public String getLetterId() {
        return CONDITIONAL_ORDER_REFUSED_LETTER;
    }
}
