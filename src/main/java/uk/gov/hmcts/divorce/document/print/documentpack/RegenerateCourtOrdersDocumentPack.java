package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID;

public class RegenerateCourtOrdersDocumentPack implements DocumentPack {

    private static final DocumentPackInfo APPLICANT_ONE_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_ONE_OFFLINE_RESPONDENT_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            Optional.of(CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID)),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APPLICANT_ONE_JS_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APPLICANT_ONE_JS_SOL_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APPLICANT_TWO_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APPLICANT_TWO_OFFLINE_RESPONDENT_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            Optional.of(CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID)),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APPLICANT_TWO_JS_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APPLICANT_TWO_JS_SOL_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of()
    );



    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        return null;
    }

    @Override
    public String getLetterId() {
        return null;
    }
}
