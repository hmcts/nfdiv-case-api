package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;

public class RegenerateCourtOrdersDocumentPack implements DocumentPack {

    private static final DocumentPackInfo APPLICANT_ONE_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_ONE_JS_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_ONE_SEPARATION_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID,
            SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_ONE_JS_SOL_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_ONE_SOL_SEPARATION_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_TWO_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_TWO_OFFLINE_RESPONDENT_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            Optional.of(CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID)),
        ImmutableMap.of(CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_TWO_JS_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_TWO_SEPARATION_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID,
            SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_TWO_JS_SOL_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT_TWO_SOL_SEPARATION_DOCUMENT_PACK = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME)
    );



    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        boolean isApplicant1 = applicant.equals(caseData.getApplicant1());
        boolean isOfflineRespondent = caseData.getApplicant2().isApplicantOffline();
        boolean isJudicialSeparation = caseData.isJudicialSeparationCase();
        boolean isRepresented = applicant.isRepresented();
        boolean isDivorce = caseData.isDivorce();

        if (isApplicant1) {
            if (isJudicialSeparation) {
                if (isDivorce) {
                    return isRepresented ? APPLICANT_ONE_JS_SOL_DOCUMENT_PACK : APPLICANT_ONE_JS_DOCUMENT_PACK;
                } else {
                    return isRepresented ? APPLICANT_ONE_SOL_SEPARATION_PACK : APPLICANT_ONE_SEPARATION_DOCUMENT_PACK;
                }
            } else {
                return APPLICANT_ONE_DOCUMENT_PACK;
            }
        } else {
            if (isJudicialSeparation) {
                if (isDivorce) {
                    return isRepresented ? APPLICANT_TWO_JS_SOL_DOCUMENT_PACK : APPLICANT_TWO_JS_DOCUMENT_PACK;
                } else {
                    return isRepresented ? APPLICANT_TWO_SOL_SEPARATION_DOCUMENT_PACK : APPLICANT_TWO_SEPARATION_DOCUMENT_PACK;
                }
            } else {
                return isOfflineRespondent ? APPLICANT_TWO_OFFLINE_RESPONDENT_DOCUMENT_PACK : APPLICANT_TWO_DOCUMENT_PACK;
            }
        }
    }

    @Override
    public String getLetterId() {
        return null;
    }
}
