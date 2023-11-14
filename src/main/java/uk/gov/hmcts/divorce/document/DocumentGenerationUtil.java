package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPack;

import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RequiredArgsConstructor
@Component
public class DocumentGenerationUtil {

    private DocumentGenerator documentGenerator;

    public void removeExistingAndGenerateNewDocuments(CaseData caseData, long caseId,
                                                          DocumentPack documentPack, List<DocumentType> documentTypesToRemove) {
        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                    .removeIf(document -> documentTypesToRemove.contains(document.getValue().getDocumentType()));
        }

        var applicant1 = caseData.getApplicant1();
        var applicant2 = caseData.getApplicant2();

        Map.of(applicant1, applicant1.isApplicantOffline(),
                        applicant2, applicant2.isApplicantOffline() || isBlank(caseData.getApplicant2EmailAddress()))
                .entrySet().stream().filter(Map.Entry::getValue)
                .forEach(applicant -> documentGenerator.generateDocuments(caseData, caseId, applicant.getKey(),
                        documentPack.getDocumentPack(caseData, applicant.getKey())));
    }

    public void removeExistingAndGenerateDoc(CaseDetails<CaseData, State> caseDetails, DocumentType documentType,
                                                                  String templateId, String documentName) {
        final CaseData caseData = caseDetails.getData();

        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                    .removeIf(document -> documentType.equals(document.getValue().getDocumentType()));
        }

        documentGenerator.generateAndStoreCaseDocument(
                documentType,
                templateId,
                documentName,
                caseData,
                caseDetails.getId());
    }
}
