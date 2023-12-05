package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_JS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_OVERDUE_LETTER;

@Component
@RequiredArgsConstructor
public class AosOverdueDocumentPack implements DocumentPack {

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

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData,
                                            Applicant applicant) {
        return caseData.isJudicialSeparationCase()
            ? APPLICANT1_JS_AOS_OVERDUE_PACK
            : APPLICANT1_AOS_OVERDUE_PACK;
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_AOS_OVERDUE;
    }
}
