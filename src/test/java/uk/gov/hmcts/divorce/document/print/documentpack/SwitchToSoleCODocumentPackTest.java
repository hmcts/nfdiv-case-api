package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

public class SwitchToSoleCODocumentPackTest {

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

    private final SwitchToSoleCODocumentPack switchToSoleCODocumentPack = new SwitchToSoleCODocumentPack();

    @Test
    public void shouldReturnSwitchToSoleCoDocumentPack() {
        CaseData data = validApplicant1CaseData();
        var documentPack = switchToSoleCODocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(SWITCH_TO_SOLE_CO_PACK);
    }

    @Test
    public void shouldReturnSwitchToSoleCoJsDocumentPack() {
        CaseData data = validApplicant1CaseData();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        var documentPack = switchToSoleCODocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(SWITCH_TO_SOLE_CO_JS_PACK);
    }

    @Test
    public void shouldReturnJsSwitchToSoleSolicitorCoDocumentPack() {
        CaseData data = validApplicant1CaseData();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        var documentPack = switchToSoleCODocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(JUDICIAL_SEPARATION_SWITCH_TO_SOLE_CO_SOLICITOR_PACK);
    }

    @Test
    public void shouldReturnCorrectLetterId() {
        assertThat(switchToSoleCODocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_SWITCH_TO_SOLE_CO);
    }

}
