package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;

@Component
@Slf4j
@RequiredArgsConstructor
public class SaveLegalProceedingDocumentsToCaseDocuments implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        CaseData caseData = caseDetails.getData();
        CaseDocuments caseDocuments = caseData.getDocuments();
        List<ListValue<DivorceDocument>> documents = caseData.getApplicant2().getLegalProceedingDocs();

        if (!CollectionUtils.isEmpty(documents)) {
                documents.forEach(divorceDocument -> {
                    caseDocuments.setConfidentialDocumentsUploaded(CaseDocuments.addDocumentToTop(
                        caseDocuments.getConfidentialDocumentsUploaded(),
                        mapToConfidentialDivorceDocument(divorceDocument.getValue()),
                        divorceDocument.getId()));
                });
            caseData.getApplicant2().setLegalProceedingDocs(null);
        }

        return caseDetails;
    }

    private ConfidentialDivorceDocument mapToConfidentialDivorceDocument(final DivorceDocument divorceDocument) {
        return ConfidentialDivorceDocument.builder()
            .documentLink(divorceDocument.getDocumentLink())
            .documentComment(divorceDocument.getDocumentComment())
            .documentFileName(divorceDocument.getDocumentFileName())
            .documentDateAdded(divorceDocument.getDocumentDateAdded())
            .documentEmailContent(divorceDocument.getDocumentEmailContent())
            .confidentialDocumentsReceived(getConfidentialDocumentType(divorceDocument.getDocumentType()))
            .build();
    }
}
