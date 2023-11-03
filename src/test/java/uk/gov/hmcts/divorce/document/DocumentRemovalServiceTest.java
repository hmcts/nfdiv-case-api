package uk.gov.hmcts.divorce.document;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
public class DocumentRemovalServiceTest {

    @Mock
    private DocumentManagementClient documentManagementClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private DocumentRemovalService documentRemovalService;

    @Test
    public void shouldRemoveDraftApplicationDocumentFromCaseDataAndDeleteApplicationDocumentFromDocManagement() {
        final List<String> systemRoles = List.of("caseworker-divorce");
        final String systemRolesCsv = String.join(",", systemRoles);
        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(APPLICATION);
        final String userId = UUID.randomUUID().toString();
        final User systemUser = systemUser(systemRoles, userId);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final String documentUuid = FilenameUtils.getName(divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        doNothing().when(documentManagementClient).deleteDocument(
            SYSTEM_USER_USER_ID,
            TEST_SERVICE_AUTH_TOKEN,
            systemRolesCsv,
            userId,
            documentUuid,
            true
        );

        documentRemovalService.deleteDocumentFromDocumentStore(singletonList(divorceDocumentListValue));

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(authTokenGenerator).generate();
        verify(documentManagementClient).deleteDocument(
            SYSTEM_USER_USER_ID,
            TEST_SERVICE_AUTH_TOKEN,
            systemRolesCsv,
            userId,
            documentUuid,
            true
        );

        verifyNoMoreInteractions(idamService, authTokenGenerator, documentManagementClient);
    }

    private User systemUser(final List<String> solicitorRoles, final String userId) {
        final UserDetails userDetails = UserDetails
            .builder()
            .roles(solicitorRoles)
            .id(userId)
            .build();

        return new User(SYSTEM_USER_USER_ID, userDetails);
    }
}
