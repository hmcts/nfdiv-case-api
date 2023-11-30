package uk.gov.hmcts.divorce.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

@Service
@Slf4j
public class DocumentRemovalService {

    @Autowired
    private CaseDocumentAccessManagement documentManagementClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    public void deleteDocument(final List<ListValue<DivorceDocument>> documentsToRemove) {

        final var systemUser = idamService.retrieveSystemUpdateUserDetails();

        documentsToRemove.stream().parallel().forEach(document -> {
            documentManagementClient.deleteDocument(
                systemUser.getAuthToken(),
                authTokenGenerator.generate(),
                document.getValue().getDocumentLink(),
                true
            );
        });
    }

    public void deleteScannedDocuments(final List<ListValue<ScannedDocument>> scannedDocsToRemove) {

        final var systemUser = idamService.retrieveSystemUpdateUserDetails();

        scannedDocsToRemove.stream().parallel().forEach(document -> {
            documentManagementClient.deleteDocument(
                systemUser.getAuthToken(),
                authTokenGenerator.generate(),
                document.getValue().getUrl(),
                true
            );
        });
    }
}
