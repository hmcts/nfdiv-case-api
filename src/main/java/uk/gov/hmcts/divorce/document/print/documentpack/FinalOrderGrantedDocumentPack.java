package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;

@Component
@RequiredArgsConstructor
public class FinalOrderGrantedDocumentPack implements DocumentPack {

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

    @Override
    public DocumentPackInfo getDocumentPack(final CaseData caseData, final Applicant applicant) {
        final boolean isApplicant1 = caseData.getApplicant1().equals(applicant);

        return isApplicant1 ? APPLICANT_1_FINAL_ORDER_PACK : APPLICANT_2_FINAL_ORDER_PACK;
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_FINAL_ORDER_GRANTED;
    }
}
