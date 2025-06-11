package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

        documentsToRemove.stream()
            .filter(document -> document.getValue().getDocumentLink() != null)
            .parallel().forEach(document ->
                documentManagementClient.deleteDocument(
                    systemUser.getAuthToken(),
                    authTokenGenerator.generate(),
                    document.getValue().getDocumentLink(),
                    true
                )
            );
    }

    public void handleDeletionOfScannedDocuments(CaseData beforeCaseData, CaseData currentCaseData) {

        List<Document> scannedDocsToRemove = new ArrayList<>(
            findScannedDocumentsForRemoval(
                beforeCaseData.getDocuments().getScannedDocuments(),
                currentCaseData.getDocuments().getScannedDocuments()
            ));

        if (!scannedDocsToRemove.isEmpty()) {
            deleteDocuments(scannedDocsToRemove);
        }
    }

    private List<Document> findScannedDocumentsForRemoval(final List<ListValue<ScannedDocument>> beforeScannedDocs,
                                                                            final List<ListValue<ScannedDocument>> afterScannedDocs) {
        if (beforeScannedDocs == null || afterScannedDocs == null) {
            return Collections.emptyList();
        }

        List<Document> beforeDocs = mapScannedDocumentsToUrls(beforeScannedDocs);
        List<Document> afterDocs = mapScannedDocumentsToUrls(afterScannedDocs);

        return beforeDocs.stream()
            .filter(document -> !afterDocs.contains(document))
            .toList();
    }

    private List<Document> mapScannedDocumentsToUrls(List<ListValue<ScannedDocument>> scannedDocuments) {
        return scannedDocuments.stream()
            .map(ListValue::getValue)
            .filter(Objects::nonNull)
            .map(ScannedDocument::getUrl)
            .filter(Objects::nonNull)
            .toList();
    }

    private void deleteDocuments(final List<Document> documents) {
        final var systemUser = idamService.retrieveSystemUpdateUserDetails();

        documents.stream()
            .parallel().forEach(document -> {
                log.info("Deleting document: {}", document.getFilename());

                documentManagementClient.deleteDocument(
                    systemUser.getAuthToken(),
                    authTokenGenerator.generate(),
                    document,
                    false
                );
            });
    }
}
