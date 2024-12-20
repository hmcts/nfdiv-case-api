package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_AWAITING_CO_DISPUTED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_AWAITING_CO_UNDEFENDED_TEMPLATE_ID;


@Component
@RequiredArgsConstructor
public class AosResponseAwaitingConditionalOrderDocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_AOS_AWAITING_CO_RESPONSE_PACK = "aos-awaiting-co-response-pack";

    static final DocumentPackInfo DISPUTED_AOS_AWAITING_CO_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(RESPONDENT_RESPONDED_AWAITING_CO_DISPUTED_TEMPLATE_ID),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            RESPONDENT_RESPONDED_AWAITING_CO_DISPUTED_TEMPLATE_ID, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo UNDISPUTED_AOS_AWAITING_CO_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(RESPONDENT_RESPONDED_AWAITING_CO_UNDEFENDED_TEMPLATE_ID),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            RESPONDENT_RESPONDED_AWAITING_CO_UNDEFENDED_TEMPLATE_ID, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        return caseData.getAcknowledgementOfService().isDisputed()
            ? DISPUTED_AOS_AWAITING_CO_RESPONSE_PACK
            : UNDISPUTED_AOS_AWAITING_CO_RESPONSE_PACK;
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_AOS_AWAITING_CO_RESPONSE_PACK;
    }
}
