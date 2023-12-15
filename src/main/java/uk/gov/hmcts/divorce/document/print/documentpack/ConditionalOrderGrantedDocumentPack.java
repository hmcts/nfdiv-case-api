package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;

@Component
@RequiredArgsConstructor
public class ConditionalOrderGrantedDocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_CONDITIONAL_ORDER_GRANTED = "conditional-order-granted-letter";

    private static final DocumentPackInfo APPLICANT_1_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_2_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED,Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_1_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED,Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_2_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED,Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_1_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID, SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_2_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED,Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID, SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_1_SOL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID, SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_2_SOL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID, SEPARATION_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_1_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME,
            CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID, CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_2_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME,
            CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID, CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME
        )
    );
    @Override
    public DocumentPackInfo getDocumentPack(final CaseData caseData, final Applicant applicant) {
        final boolean isApplicant1 = caseData.getApplicant1().equals(applicant);

        if (caseData.isJudicialSeparationCase()) {
            if (isApplicant1) {
                if (applicant.isRepresented()) {
                    if (caseData.isDivorce()) {
                        //Judicial Separation
                        return APPLICANT_1_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK;
                    } else {
                        //Separation
                        return APPLICANT_1_SOL_SEPARATION_CONDITIONAL_ORDER_PACK;
                    }
                }
                else {
                    if (caseData.isDivorce()) {
                        //Judicial Separation
                        return APPLICANT_1_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK;
                    } else {
                        //Separation
                        return APPLICANT_1_SEPARATION_CONDITIONAL_ORDER_PACK;
                    }
                }
            } else {
                if (applicant.isRepresented()) {
                    if (caseData.isDivorce()) {
                        //Judicial Separation
                        return APPLICANT_2_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK;
                    } else {
                        //Separation
                        return APPLICANT_2_SOL_SEPARATION_CONDITIONAL_ORDER_PACK;
                    }
                }
                else{
                    if (caseData.isDivorce()) {
                        //Judicial Separation
                        return APPLICANT_2_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK;
                    } else {
                        //Separation
                        return APPLICANT_2_SEPARATION_CONDITIONAL_ORDER_PACK;
                    }
                }
            }
        } else {
            if (isApplicant1) {
                return APPLICANT_1_CONDITIONAL_ORDER_PACK;
            } else {
                return APPLICANT_2_CONDITIONAL_ORDER_PACK;
            }
        }
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_CONDITIONAL_ORDER_GRANTED;
    }
}
