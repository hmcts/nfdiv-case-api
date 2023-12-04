package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

public class FinalOrderGrantedDocumentPackTest {

    private static final String LETTER_TYPE_FINAL_ORDER_GRANTED = "final-order-granted-letter";

    private static final DocumentPackInfo APPLICANT_1_FINAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_1, Optional.of(FINAL_ORDER_COVER_LETTER_TEMPLATE_ID),
            FINAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            FINAL_ORDER_COVER_LETTER_TEMPLATE_ID, FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APPLICANT_2_FINAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_2, Optional.of(FINAL_ORDER_COVER_LETTER_TEMPLATE_ID),
            FINAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            FINAL_ORDER_COVER_LETTER_TEMPLATE_ID, FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        )
    );

    private final FinalOrderGrantedDocumentPack finalOrderGrantedDocumentPack = new FinalOrderGrantedDocumentPack();

    @Test
    public void shouldReturnApplicant1DocumentPackWhenPassedApplicant1() {
        CaseData data = validApplicant1CaseData();
        var documentPack = finalOrderGrantedDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(APPLICANT_1_FINAL_ORDER_PACK);
    }

    @Test
    public void shouldReturnApplicant2DocumentPackWhenPassedApplicant2() {
        CaseData data = validApplicant1CaseData();
        var documentPack = finalOrderGrantedDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(APPLICANT_2_FINAL_ORDER_PACK);
    }

    @Test
    public void shouldReturnCorrectLetterId() {
        assertThat(finalOrderGrantedDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_FINAL_ORDER_GRANTED);
    }

}
