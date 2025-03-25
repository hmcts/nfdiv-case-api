package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentRemovalService {

    private final CaseDocumentAccessManagement documentManagementClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final IdamService idamService;

    public void deleteDocument(final Document document) {
        final var systemUser = idamService.retrieveSystemUpdateUserDetails();

        documentManagementClient.deleteDocument(
            systemUser.getAuthToken(),
            authTokenGenerator.generate(),
            document,
            true
        );
    }

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

    public void handleDeletionOfScannedDocuments(CaseData beforeCaseData, CaseData currentCaseData) {

        List<ListValue<ScannedDocument>> scannedDocsToRemove = new ArrayList<>(
            findScannedDocumentsForRemoval(
                beforeCaseData.getDocuments().getScannedDocuments(),
                currentCaseData.getDocuments().getScannedDocuments()
            ));

        if (!scannedDocsToRemove.isEmpty()) {
            deleteScannedDocuments(scannedDocsToRemove);
        }
    }

    private List<ListValue<ScannedDocument>> findScannedDocumentsForRemoval(final List<ListValue<ScannedDocument>> beforeDocs,
                                                                            final List<ListValue<ScannedDocument>> currentDocs) {

        List<ListValue<ScannedDocument>> scannedDocsToRemove = new ArrayList<>();

        if (beforeDocs != null && currentDocs != null) {
            beforeDocs.forEach(document -> {
                if (!currentDocs.contains(document)) {
                    scannedDocsToRemove.add(document);
                }
            });
        }

        return scannedDocsToRemove;
    }

    private void deleteScannedDocuments(final List<ListValue<ScannedDocument>> scannedDocsToRemove) {

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
