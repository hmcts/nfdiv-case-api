package uk.gov.hmcts.divorce.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;

@Service
@Slf4j
public class DraftApplicationRemovalService {

    @Autowired
    private DocumentManagementClient documentManagementClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    public List<ListValue<DivorceDocument>> removeDraftApplicationDocument(
        final List<ListValue<DivorceDocument>> generatedDocuments,
        final Long caseId
    ) {

        if (isEmpty(generatedDocuments)) {
            log.info("Generated documents list is empty for case id {} ", caseId);
            return emptyList();
        }

        final User systemUser = idamService.retrieveSystemUpdateUserDetails();

        final List<ListValue<DivorceDocument>> generatedDocumentsExcludingApplication = generatedDocuments
            .stream()
            .map(document ->
                deleteDocumentFromDocumentStore(
                    document,
                    systemUser,
                    String.valueOf(caseId)
                )
            )
            .filter(document -> !isApplicationDocument(document))
            .collect(toList());


        log.info("Successfully removed application document from case data generated document list for case id {} ", caseId);

        return generatedDocumentsExcludingApplication;
    }

    private ListValue<DivorceDocument> deleteDocumentFromDocumentStore(
        final ListValue<DivorceDocument> document,
        final User user,
        final String caseId
    ) {
        if (isApplicationDocument(document)) {

            final UserDetails userDetails = user.getUserDetails();
            final String rolesCsv = String.join(",", userDetails.getRoles());

            documentManagementClient.deleteDocument(
                user.getAuthToken(),
                authTokenGenerator.generate(),
                rolesCsv,
                userDetails.getId(),
                FilenameUtils.getName(document.getValue().getDocumentLink().getUrl()),
                true
            );
            log.info("Successfully deleted application document from document management for case id {} ", caseId);
        } else {
            log.info("No draft application document found for case id {} ", caseId);
        }
        return document;
    }

    private boolean isApplicationDocument(ListValue<DivorceDocument> document) {
        return document.getValue().getDocumentType().equals(DIVORCE_APPLICATION);
    }
}
