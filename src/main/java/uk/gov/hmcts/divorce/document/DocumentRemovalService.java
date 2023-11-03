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

@Service
@Slf4j
public class DocumentRemovalService {

    @Autowired
    private DocumentManagementClient documentManagementClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    public void deleteDocumentFromDocumentStore(final List<ListValue<DivorceDocument>> documentsToRemove) {

        final User systemUser = idamService.retrieveSystemUpdateUserDetails();
        final UserDetails userDetails = systemUser.getUserDetails();
        final String rolesCsv = String.join(",", userDetails.getRoles());

        documentsToRemove.stream().parallel().forEach(document -> {
            documentManagementClient.deleteDocument(
                systemUser.getAuthToken(),
                authTokenGenerator.generate(),
                rolesCsv,
                userDetails.getId(),
                FilenameUtils.getName(document.getValue().getDocumentLink().getUrl()),
                true
            );
        });
    }
}
