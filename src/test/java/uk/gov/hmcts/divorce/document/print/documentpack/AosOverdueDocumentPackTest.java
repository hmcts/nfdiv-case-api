package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_JS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_OVERDUE_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

public class AosOverdueDocumentPackTest {

    private static final String LETTER_TYPE_AOS_OVERDUE = "aos-overdue";

    private static final DocumentPackInfo APPLICANT1_AOS_OVERDUE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            AOS_OVERDUE_LETTER, Optional.of(AOS_OVERDUE_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            AOS_OVERDUE_TEMPLATE_ID, AOS_OVERDUE_LETTER_DOCUMENT_NAME)
    );

    private static final DocumentPackInfo APPLICANT1_JS_AOS_OVERDUE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            AOS_OVERDUE_LETTER, Optional.of(AOS_OVERDUE_JS_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            AOS_OVERDUE_JS_TEMPLATE_ID, AOS_OVERDUE_LETTER_DOCUMENT_NAME)
    );

    private final AosOverdueDocumentPack aosOverdueDocumentPack = new AosOverdueDocumentPack();

    @Test
    public void shouldReturnApplicant1DocumentPackForDivorceCase() {
        CaseData data = validApplicant1CaseData();
        var documentPack = aosOverdueDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(APPLICANT1_AOS_OVERDUE_PACK);
    }

    @Test
    public void shouldReturnApplicant1JSDocumentPackWhenJudicialSeparation() {
        CaseData data = validApplicant1CaseData();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        var documentPack = aosOverdueDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(APPLICANT1_JS_AOS_OVERDUE_PACK);
    }

    @Test
    public void shouldReturnCorrectLetterId() {
        assertThat(aosOverdueDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_AOS_OVERDUE);
    }
}
