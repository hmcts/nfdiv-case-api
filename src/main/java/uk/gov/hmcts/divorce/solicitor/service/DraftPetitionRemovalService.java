package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.DocumentManagementClient;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.model.DocumentType.PETITION;

@Service
@Slf4j
public class DraftPetitionRemovalService {

    @Autowired
    private DocumentManagementClient documentManagementClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    public List<ListValue<DivorceDocument>> removeDraftPetitionDocument(
        List<ListValue<DivorceDocument>> generatedDocuments,
        Long caseId,
        String userAuth
    ) {

        if (isEmpty(generatedDocuments)) {
            return emptyList();
        }

        UserDetails solicitorUserDetails = idamService.retrieveUser(userAuth).getUserDetails();

        List<ListValue<DivorceDocument>> generatedDocumentsExcludingPetition = generatedDocuments
            .stream()
            .map(document ->
                deleteDocumentFromDocumentStore(
                    document,
                    userAuth,
                    String.join(",", solicitorUserDetails.getRoles()),
                    solicitorUserDetails.getId(),
                    String.valueOf(caseId)
                )
            )
            .filter(document -> !isPetitionDocument(document))
            .collect(Collectors.toList());


        log.info("Successfully removed petition document from case data generated document list for case id {} ", caseId);

        return generatedDocumentsExcludingPetition;
    }


    private ListValue<DivorceDocument> deleteDocumentFromDocumentStore(
        ListValue<DivorceDocument> document,
        String userAuth,
        String userRoles,
        String userId,
        String caseId
    ) {
        if (isPetitionDocument(document)) {
            documentManagementClient.deleteDocument(
                userAuth,
                authTokenGenerator.generate(),
                userRoles,
                userId,
                document.getValue().getDocumentLink().getUrl(),
                true
            );
            log.info("Successfully deleted petition document from document management for case id {} ", caseId);
        }
        return document;
    }

    private boolean isPetitionDocument(ListValue<DivorceDocument> document) {
        return document.getValue().getDocumentType().equals(PETITION);
    }
}
