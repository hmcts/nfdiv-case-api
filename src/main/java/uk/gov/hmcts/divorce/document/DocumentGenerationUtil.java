package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Component
public class DocumentGenerationUtil {
    public void removeExistingGeneratedDocuments(CaseData caseData, List<DocumentType> documentTypesToRemove) {
        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                    .removeIf(document -> documentTypesToRemove.contains(document.getValue().getDocumentType()));
        }
    }
}
