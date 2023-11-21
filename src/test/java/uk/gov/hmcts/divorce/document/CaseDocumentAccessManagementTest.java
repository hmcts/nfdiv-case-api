package uk.gov.hmcts.divorce.document;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

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

        verify(client).uploadDocuments(eq("dummy"), eq("dummy"), eq(getCaseType()), eq(JURISDICTION), anyList());
    }

    @Test
    void downloadBinary() {
        var doc = documentWithType(APPLICATION);
        var url = doc.getValue().getDocumentLink().getUrl();
        var uuid = UUID.fromString(url.substring(url.lastIndexOf('/') + 1));

        cdam.downloadBinary("dummy", "dummy", doc.getValue().getDocumentLink());
        verify(client).getDocumentBinary("dummy", "dummy", uuid);
    }

    @Test
    void ignore404ErrorFromCdam() {

        var doc = documentWithType(APPLICATION);
        var url = doc.getValue().getDocumentLink().getUrl();
        var uuid = UUID.fromString(url.substring(url.lastIndexOf('/') + 1));

        doThrow(feignException(404, "NOT FOUND"))
            .when(client).deleteDocument("dummy", "dummy", uuid, true);

        cdam.deleteDocument("dummy", "dummy", doc.getValue().getDocumentLink(), true);
        verify(client).deleteDocument("dummy", "dummy", uuid, true);
    }

    @Test
    void rethrowExceptionIfNot404WhenCallingToDeleteDocumentFromCdam() {

        var doc = documentWithType(APPLICATION);
        var url = doc.getValue().getDocumentLink().getUrl();
        var uuid = UUID.fromString(url.substring(url.lastIndexOf('/') + 1));

        doThrow(feignException(401, "some error"))
            .when(client).deleteDocument("dummy", "dummy", uuid, true);

        final FeignException exception = assertThrows(
            FeignException.class,
            () -> cdam.deleteDocument("dummy", "dummy", doc.getValue().getDocumentLink(), true));

        assertThat(exception.getMessage()).contains("some error");
    }
}
