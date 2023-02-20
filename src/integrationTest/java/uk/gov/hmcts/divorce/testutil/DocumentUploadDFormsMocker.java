package uk.gov.hmcts.divorce.testutil;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Component
public class DocumentUploadDFormsMocker {

    @Autowired
    private DocumentUploadClientApi documentUploadClientApi;

    public void mockDFormsUpload(DocumentType docType, String docTemplateId) {
        UploadResponse uploadResponse = Mockito.mock(UploadResponse.class);
        UploadResponse.Embedded embedded = Mockito.mock(UploadResponse.Embedded.class);
        when(uploadResponse.getEmbedded()).thenReturn(embedded);
        Document doc = new Document();
        doc.links = new Document.Links();
        doc.links.self = new Document.Link();
        doc.links.self.href = "http://dm-store-aat.service.core-compute-aat.internal/documents/" + docTemplateId;
        doc.links.binary = new Document.Link();
        doc.links.binary.href = "http://dm-store-aat.service.core-compute-aat.internal/documents/" + docTemplateId + "/binary";
        doc.originalDocumentName = docType.getLabel() + ".pdf";
        List<Document> formDocList = asList(doc);
        when(embedded.getDocuments()).thenReturn(formDocList);
        when(documentUploadClientApi.upload(anyString(),
            anyString(),
            anyString(),
            anyList()
        )).thenReturn(uploadResponse);
    }
}
