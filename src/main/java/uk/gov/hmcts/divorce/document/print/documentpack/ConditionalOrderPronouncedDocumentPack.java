package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_SOL_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;

@Component
@RequiredArgsConstructor
public class ConditionalOrderPronouncedDocumentPack implements DocumentPack {
    public static final String LETTER_TYPE_CO_PRONOUNCED = "conditional-order-pronounced";


    private static final DocumentPackInfo APPLICANT_1_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_1_DIV_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_1_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_1_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_1_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_1_SOLE_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_2_DIV_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_SOLE_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_1_SOL_SOLE_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_SOL_SOLE_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_SOL_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_SOL_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_1_SOL_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_SOL_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );

    @Override
    public String getLetterId() {
        return LETTER_TYPE_CO_PRONOUNCED;
    }

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        // only called for offline applicant
        boolean isJudicialSeparation = caseData.isJudicialSeparationCase();
        boolean isApplicant1 = applicant.equals(caseData.getApplicant1());
        boolean isRepresented = applicant.isRepresented();
        boolean isDivorce = caseData.isDivorce();
        boolean isSole = caseData.getApplicationType().isSole();

        if (isJudicialSeparation) {
            if (isDivorce) {
                if (isRepresented) {
                    return isApplicant1 ? APPLICANT_1_DIV_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK :
                        APPLICANT_2_DIV_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK;
                } else {
                    return isApplicant1 ? APPLICANT_1_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK :
                        APPLICANT_2_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK;
                }
            } else {
                if (isRepresented) {
                    return isApplicant1 ? APPLICANT_1_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK :
                        APPLICANT_2_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK;
                } else {
                    return isApplicant1 ? APPLICANT_1_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK :
                        APPLICANT_2_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK;
                }
            }
        } else {
            if (isSole) {
                if (isRepresented) {
                    return isApplicant1 ? APPLICANT_1_SOL_SOLE_CONDITIONAL_ORDER_PACK :
                        APPLICANT_2_SOL_SOLE_CONDITIONAL_ORDER_PACK;
                } else {
                    return isApplicant1 ? APPLICANT_1_SOLE_CONDITIONAL_ORDER_PACK :
                        APPLICANT_2_SOLE_CONDITIONAL_ORDER_PACK;
                }
            } else {
                if (isRepresented) {
                    return isApplicant1 ? APPLICANT_1_SOL_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK :
                        APPLICANT_2_SOL_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK;
                } else {
                    return isApplicant1 ? APPLICANT_1_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK :
                        APPLICANT_2_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK;
                }
            }
        }
    }
}


