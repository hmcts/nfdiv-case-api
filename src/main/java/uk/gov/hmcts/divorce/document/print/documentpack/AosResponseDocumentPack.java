package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_UNDISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID;


@Component
@RequiredArgsConstructor
public class AosResponseDocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_AOS_RESPONSE_PACK = "aos-response-pack";

    private final GenerateD10Form generateD10Form;
    private final GenerateD84Form generateD84Form;

    static final DocumentPackInfo SOLICITOR_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo APP_2_OFFLINE_SOLICITOR_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty(),
            DocumentType.D84, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo APP_2_OFFLINE_CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty(),
            DocumentType.D84, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_JS_SOLE_UNDISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_JS_SOLE_UNDISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo APP_2_OFFLINE_SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty(),
            DocumentType.D84, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo APP_2_OFFLINE_CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_JS_SOLE_UNDISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty(),
            DocumentType.D84, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_JS_SOLE_UNDISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        final AcknowledgementOfService acknowledgementOfService = caseData.getAcknowledgementOfService();
        final boolean isApp2Offline = caseData.getApplicant2().isApplicantOffline();

        if (caseData.isJudicialSeparationCase()) {
            generateD84Form.generateD84(caseData);
            if (acknowledgementOfService.isDisputed()) {
                if (applicant.isRepresented()) {
                    return isApp2Offline ? APP_2_OFFLINE_SOLICITOR_JS_DISPUTED_AOS_RESPONSE_PACK : SOLICITOR_JS_DISPUTED_AOS_RESPONSE_PACK;
                } else {
                    return isApp2Offline ? APP_2_OFFLINE_CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK : CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK;
                }
            } else {
                if (applicant.isRepresented()) {
                    generateD10Form.apply(caseData);
                    return isApp2Offline ? APP_2_OFFLINE_SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK
                        : SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK;
                } else {
                    return isApp2Offline ? APP_2_OFFLINE_CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK : CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK;
                }
            }
        } else {
            return acknowledgementOfService.isDisputed() ? DISPUTED_AOS_RESPONSE_PACK : UNDISPUTED_AOS_RESPONSE_PACK;
        }
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_AOS_RESPONSE_PACK;
    }
}
