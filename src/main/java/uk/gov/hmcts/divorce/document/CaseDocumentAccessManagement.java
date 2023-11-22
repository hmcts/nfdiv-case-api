package uk.gov.hmcts.divorce.document;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;

@Service
@AllArgsConstructor
public class CaseDocumentAccessManagement {

    private CaseDocumentClient client;

    public UploadResponse upload(final String userToken,
                                 final String serviceToken,
                                 final String displayName,
                                 final String fileName,
                                 final String filePath) throws IOException {

        final var file = IOUtils.resourceToByteArray(filePath);

        return client.uploadDocuments(
            userToken,
            serviceToken,
            getCaseType(),
            JURISDICTION,
            List.of(
                new InMemoryMultipartFile(
                    displayName,
                    fileName,
                    MediaType.APPLICATION_PDF_VALUE,
                    file
                )
            )
        );
    }

    public void deleteDocument(String userToken, String serviceToken, Document document, boolean hard) {
        try {
            client.deleteDocument(userToken, serviceToken, getUuid(document), hard);
        } catch (FeignException e) {
            // Ignore 404 as document is already deleted if returned.
            if (e.status() != 404) {
                throw e;
            }
        }
    }

    public ResponseEntity<Resource> downloadBinary(String userAuth, String serviceAuth, Document document) {
        return client.getDocumentBinary(userAuth, serviceAuth, getUuid(document));
    }

    private UUID getUuid(Document document) {
        return UUID.fromString(document.getUrl().substring(document.getUrl().lastIndexOf('/') + 1));
    }
}
