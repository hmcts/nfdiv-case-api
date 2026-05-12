package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SOLE_APPLICANT_FINAL_ORDER_OVERDUE_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SOLE_APPLICANT_FINAL_ORDER_OVERDUE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D36;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SOLE_APPLICANT_FINAL_ORDER_OVERDUE;

@Component
@RequiredArgsConstructor
public class SoleApplicantFinalOrderOverdueDocumentPack implements DocumentPack {

    static final String SOLE_APPLICANT_FINAL_ORDER_OVERDUE_PACK = "sole-applicant-final-order-overdue-pack";

    static final DocumentPackInfo APPLICANT1_OFFLINE_DOCUMENTPACK = new DocumentPackInfo(
        ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            SOLE_APPLICANT_FINAL_ORDER_OVERDUE, Optional.of(SOLE_APPLICANT_FINAL_ORDER_OVERDUE_TEMPLATE_ID),
            D36, Optional.empty() // empty optional if generated from case data for same doc send ti mulitple apps
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            SOLE_APPLICANT_FINAL_ORDER_OVERDUE_TEMPLATE_ID, SOLE_APPLICANT_FINAL_ORDER_OVERDUE_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        return APPLICANT1_OFFLINE_DOCUMENTPACK;
    }

    @Override
    public String getLetterId() {
        return SOLE_APPLICANT_FINAL_ORDER_OVERDUE_PACK;
    }
}
