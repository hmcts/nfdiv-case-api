package uk.gov.hmcts.divorce.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
class CaseDocumentAccessManagementTest {

    @Mock
    CaseDocumentClient client;

    @InjectMocks
    private CaseDocumentAccessManagement cdam;

    @Test
    void deleteDocument() {
        var doc = documentWithType(APPLICATION);
        var url = doc.getValue().getDocumentLink().getUrl();
        var uuid = UUID.fromString(url.substring(url.lastIndexOf('/') + 1));

        cdam.deleteDocument("dummy", "dummy", doc.getValue().getDocumentLink(), true);
        verify(client).deleteDocument("dummy", "dummy", uuid, true);
    }

    @Test
    void upload() throws IOException {
        when(client.uploadDocuments(eq("dummy"), eq("dummy"), eq(getCaseType()), eq(JURISDICTION), anyList()))
            .thenReturn(mock(UploadResponse.class));

        cdam.upload("dummy", "dummy", "displayName", "fileName", "/D10.pdf");
    }

    @Test
    void downloadBinary() {
        var doc = documentWithType(APPLICATION);
        var url = doc.getValue().getDocumentLink().getUrl();
        var uuid = UUID.fromString(url.substring(url.lastIndexOf('/') + 1));

        cdam.downloadBinary("dummy", "dummy", doc.getValue().getDocumentLink());
        verify(client).getDocumentBinary("dummy", "dummy", uuid);
    }
}
