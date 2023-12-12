package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLIED_FOR_CO_LETTER;

@Component
@RequiredArgsConstructor
public class AppliedForConditionalOrderDocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_APPLIED_FOR_CO = "applied-for-co-letter";

    private static final DocumentPackInfo APPLIED_FOR_CO_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            APPLIED_FOR_CO_LETTER, Optional.of(APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID, APPLIED_FOR_CONDITIONAL_ORDER_LETTER_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        return APPLIED_FOR_CO_PACK;
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_APPLIED_FOR_CO;
    }
}
