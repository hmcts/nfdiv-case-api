package uk.gov.hmcts.divorce.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
public class DocumentRemovalService {

    @Autowired
    private DocumentManagementClient documentManagementClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    public void deleteDocumentFromDocumentStore(
        final List<ListValue<DivorceDocument>> documents,
        final DocumentType documentTypeToRemove,
        final Long caseId
    ) {
        if (!isEmpty(documents)) {

            List<ListValue<DivorceDocument>> documentsToRemove = documents.stream()
                .filter(document -> documentTypeToRemove.equals(document.getValue().getDocumentType()))
                .toList();

            if (!CollectionUtils.isEmpty(documentsToRemove)) {
                final User systemUser = idamService.retrieveSystemUpdateUserDetails();
                final String rolesCsv = String.join(",", systemUser.getUserDetails().getRoles());

                documentsToRemove
                    .forEach(document ->
                        documentManagementClient.deleteDocument(
                            systemUser.getAuthToken(),
                            authTokenGenerator.generate(),
                            rolesCsv,
                            systemUser.getUserDetails().getId(),
                            FilenameUtils.getName(document.getValue().getDocumentLink().getUrl()),
                            true
                        ));

                log.info("Successfully deleted {} documents from document management for case id {} ",
                    documentTypeToRemove.getLabel(), caseId);
            } else {
                log.info("{} type documents not found in the given list for deletion", documentTypeToRemove.getLabel());
            }

        } else {
            log.info("Documents list is empty for case id {} ", caseId);
        }
    }
}
