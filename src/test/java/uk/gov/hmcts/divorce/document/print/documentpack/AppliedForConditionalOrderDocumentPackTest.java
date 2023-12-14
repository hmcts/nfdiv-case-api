package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLIED_FOR_CO_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

public class AppliedForConditionalOrderDocumentPackTest {

    private static final String LETTER_TYPE_APPLIED_FOR_CO = "applied-for-co-letter";

    private static final DocumentPackInfo APPLIED_FOR_CO_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            APPLIED_FOR_CO_LETTER, Optional.of(APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID, APPLIED_FOR_CONDITIONAL_ORDER_LETTER_DOCUMENT_NAME
        )
    );

    private final AppliedForConditionalOrderDocumentPack appliedForCoDocumentPack = new AppliedForConditionalOrderDocumentPack();

    @Test
    public void shouldReturnDocumentPack() {
        CaseData data = validApplicant1CaseData();
        var documentPack = appliedForCoDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(APPLIED_FOR_CO_PACK);
    }

    @Test
    public void shouldReturnCorrectLetterId() {
        assertThat(appliedForCoDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_APPLIED_FOR_CO);
    }

}
