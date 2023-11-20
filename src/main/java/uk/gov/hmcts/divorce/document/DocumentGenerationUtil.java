package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;

@RequiredArgsConstructor
@Component
public class DocumentGenerationUtil {

    private final DocumentGenerator documentGenerator;

    public void removeExistingDocuments(CaseData caseData, List<DocumentType> documentTypesToRemove) {
        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                    .removeIf(document -> documentTypesToRemove.contains(document.getValue().getDocumentType()));
        }
    }

    public void generateCertificateOfEntitlement(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();

        documentGenerator.generateAndStoreCaseDocument(
                CERTIFICATE_OF_ENTITLEMENT,
                caseData.isJudicialSeparationCase()
                        ? CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID
                        : CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID,
                CERTIFICATE_OF_ENTITLEMENT_NAME,
                caseData,
                details.getId()
        );

        var regeneratedCertificateOfEntitlement =
                caseData.getDocuments().getDocumentsGenerated()
                        .stream()
                        .filter(doc -> doc.getValue().getDocumentType().equals(CERTIFICATE_OF_ENTITLEMENT))
                        .findFirst().orElseThrow();

        caseData.getConditionalOrder().setCertificateOfEntitlementDocument(regeneratedCertificateOfEntitlement.getValue());
    }
}
