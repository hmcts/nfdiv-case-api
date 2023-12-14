package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;

@Component
@RequiredArgsConstructor
public class SwitchToSoleCODocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_SWITCH_TO_SOLE_CO = "switch-to-sole-co-letter";

    private static final DocumentPackInfo SWITCH_TO_SOLE_CO_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_LETTER, Optional.of(SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo SWITCH_TO_SOLE_CO_JS_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_LETTER, Optional.of(SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo JUDICIAL_SEPARATION_SWITCH_TO_SOLE_CO_SOLICITOR_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_LETTER, Optional.of(JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        if (caseData.isJudicialSeparationCase()) {
            if (caseData.getApplicant2().isRepresented()) {
                return JUDICIAL_SEPARATION_SWITCH_TO_SOLE_CO_SOLICITOR_PACK;
            } else {
                return SWITCH_TO_SOLE_CO_JS_PACK;
            }
        } else {
            return SWITCH_TO_SOLE_CO_PACK;
        }
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_SWITCH_TO_SOLE_CO;
    }
}
