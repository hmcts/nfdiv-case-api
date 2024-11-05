package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD36Form;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D36;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_RESPONDENT;

@Component
@RequiredArgsConstructor
public class ApplyForFinalOrderDocumentPack implements DocumentPack {

    static final String LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK = "apply-for-final-order-pack";
    private final GenerateD36Form generateD36Form;

    static final DocumentPackInfo APPLICANT1_OFFLINE_DOCUMENTPACK = new DocumentPackInfo(
        ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            FINAL_ORDER_CAN_APPLY_APP1, Optional.of(FINAL_ORDER_CAN_APPLY_TEMPLATE_ID),
            D36, Optional.empty() // empty optional if generated from case data for same doc send ti mulitple apps
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            FINAL_ORDER_CAN_APPLY_TEMPLATE_ID, FINAL_ORDER_CAN_APPLY_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo APPLICANT2_OFFLINE_DOCUMENTPACK = new DocumentPackInfo(
        ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            FINAL_ORDER_CAN_APPLY_APP2, Optional.of(FINAL_ORDER_CAN_APPLY_TEMPLATE_ID),
            D36, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            FINAL_ORDER_CAN_APPLY_TEMPLATE_ID, FINAL_ORDER_CAN_APPLY_DOCUMENT_NAME
        )
    );

    static final DocumentPackInfo RESPONDENT_OFFLINE_DOCUMENTPACK = new DocumentPackInfo(
        ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            FINAL_ORDER_CAN_APPLY_RESPONDENT, Optional.of(FINAL_ORDER_CAN_APPLY_RESPONDENT_TEMPLATE_ID),
            D36, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            FINAL_ORDER_CAN_APPLY_RESPONDENT_TEMPLATE_ID, FINAL_ORDER_CAN_APPLY_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        generateD36Form.generateD36Document(caseData);
        return caseData.getApplicant1().equals(applicant) ? APPLICANT1_OFFLINE_DOCUMENTPACK :
            caseData.getApplicationType().isSole() ? RESPONDENT_OFFLINE_DOCUMENTPACK : APPLICANT2_OFFLINE_DOCUMENTPACK;
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK;
    }
}
