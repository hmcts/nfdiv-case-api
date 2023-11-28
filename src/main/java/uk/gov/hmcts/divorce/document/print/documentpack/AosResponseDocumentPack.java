package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
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
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_TEMPLATE_ID;


@Component
@RequiredArgsConstructor
public class AosResponseDocumentPack implements DocumentPack {

    private final GenerateD10Form generateD10Form;
    private final GenerateD84Form generateD84Form;

    private static final DocumentPackInfo SOLICITOR_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.of(RESPONDENT_ANSWERS_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME,
            RESPONDENT_ANSWERS_TEMPLATE_ID, RESPONDENT_ANSWERS_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.of(RESPONDENT_ANSWERS_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME,
            RESPONDENT_ANSWERS_TEMPLATE_ID, RESPONDENT_ANSWERS_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.of(RESPONDENT_ANSWERS_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME,
            RESPONDENT_ANSWERS_TEMPLATE_ID, RESPONDENT_ANSWERS_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APP_2_OFFLINE_SOLICITOR_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APP_2_OFFLINE_CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APP_2_OFFLINE_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APP_2_OFFLINE_SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APP_2_OFFLINE_CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    private static final DocumentPackInfo APP_2_OFFLINE_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
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
