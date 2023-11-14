package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;

@Component
@RequiredArgsConstructor
public class CertificateOfEntitlementDocumentPack implements DocumentPack {
    private static final String LETTER_TYPE_CERTIFICATE_OF_ENTITLEMENT = "certificate-of-entitlement";

    private static final DocumentPackInfo APPLICANT_1_CERTIFICATE_OF_ENTITLEMENT_PACK = new DocumentPackInfo(
            ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, Optional.of(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID),
                    CERTIFICATE_OF_ENTITLEMENT, Optional.empty()
            ),
            ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_NAME
            )
    );

    private static final DocumentPackInfo APPLICANT_2_CERTIFICATE_OF_ENTITLEMENT_PACK = new DocumentPackInfo(
            ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2, Optional.of(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID),
                    CERTIFICATE_OF_ENTITLEMENT, Optional.empty()
            ),
            ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_NAME
            )
    );

    private static final DocumentPackInfo APPLICANT_1_CERTIFICATE_OF_ENTITLEMENT_PACK_JS = new DocumentPackInfo(
            ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, Optional.of(CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID),
                    CERTIFICATE_OF_ENTITLEMENT, Optional.empty()
            ),
            ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_NAME
            )
    );

    private static final DocumentPackInfo APPLICANT_2_CERTIFICATE_OF_ENTITLEMENT_PACK_JS = new DocumentPackInfo(
            ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2, Optional.of(CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID),
                    CERTIFICATE_OF_ENTITLEMENT, Optional.empty()
            ),
            ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_NAME
            )
    );

    @Override
    public DocumentPackInfo getDocumentPack(final CaseData caseData, final Applicant applicant) {
        final boolean isApplicant1 = caseData.getApplicant1().equals(applicant);

        if (caseData.isJudicialSeparationCase()) {
            return isApplicant1 ? APPLICANT_1_CERTIFICATE_OF_ENTITLEMENT_PACK_JS : APPLICANT_2_CERTIFICATE_OF_ENTITLEMENT_PACK_JS;
        } else {
            return isApplicant1 ? APPLICANT_1_CERTIFICATE_OF_ENTITLEMENT_PACK : APPLICANT_2_CERTIFICATE_OF_ENTITLEMENT_PACK;
        }
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_CERTIFICATE_OF_ENTITLEMENT;
    }
}
