package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

public class ConditionalOrderGrantedDocumentPackTest {

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
    private final ConditionalOrderGrantedDocumentPack conditionalOrderGrantedDocumentPack = new ConditionalOrderGrantedDocumentPack();

    @Test
    public void shouldReturnApplicant1DocumentPackWhenPassedApplicant1() {
        CaseData data = validApplicant1CaseData();
        var documentPack = conditionalOrderGrantedDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(APPLICANT_1_CONDITIONAL_ORDER_PACK);
    }

    @Test
    public void shouldReturnApplicant2DocumentPackWhenPassedApplicant2() {
        CaseData data = validApplicant1CaseData();
        var documentPack = conditionalOrderGrantedDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(APPLICANT_2_CONDITIONAL_ORDER_PACK);
    }

    @Test
    public void shouldReturnCorrectLetterId() {
        assertThat(conditionalOrderGrantedDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_CONDITIONAL_ORDER_GRANTED);
    }

}
