package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.DocumentManagementClient;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static feign.Request.HttpMethod.DELETE;
import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
public class DraftPetitionRemovalServiceTest {

    @Mock
    private DocumentManagementClient documentManagementClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private DraftPetitionRemovalService draftPetitionRemovalService;

    @Test
    public void shouldRemoveDraftPetitionDocumentFromCaseDataAndDeletePetitionDocumentFromDocManagement() {
        List<String> solicitorRoles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");

        String solicitorRolesCsv = String.join(",", solicitorRoles);

        ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(DIVORCE_APPLICATION);

        String userId = UUID.randomUUID().toString();

        User solicitorUser = solicitorUser(solicitorRoles, userId);

        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN)).thenReturn(solicitorUser);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        String documentUuid = FilenameUtils.getName(divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        doNothing().when(documentManagementClient).deleteDocument(
            APP_1_SOL_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            solicitorRolesCsv,
            userId,
            documentUuid,
            true
        );

        List<ListValue<DivorceDocument>> actualDocumentsList = draftPetitionRemovalService.removeDraftPetitionDocument(
            singletonList(divorceDocumentListValue),
            TEST_CASE_ID,
            APP_1_SOL_AUTH_TOKEN
        );

        assertThat(actualDocumentsList).isEmpty();

        verify(idamService).retrieveUser(APP_1_SOL_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verify(documentManagementClient).deleteDocument(
            APP_1_SOL_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            solicitorRolesCsv,
            userId,
            documentUuid,
            true
        );

        verifyNoMoreInteractions(idamService, authTokenGenerator, documentManagementClient);
    }

    @Test
    public void shouldThrow403ForbiddenWhenServiceIsNotWhitelistedInDocManagement() {
        List<String> solicitorRoles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");

        String solicitorRolesCsv = String.join(",", solicitorRoles);

        ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(DIVORCE_APPLICATION);

        String userId = UUID.randomUUID().toString();

        User solicitorUser = solicitorUser(solicitorRoles, userId);

        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN)).thenReturn(solicitorUser);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        byte[] emptyBody = {};
        Request request = Request.create(DELETE, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "userRolesNotAllowedToDelete",
            Response.builder()
                .request(request)
                .status(403)
                .headers(emptyMap())
                .reason("User role is not authorised to delete document")
                .build()
        );

        doThrow(feignException)
            .when(documentManagementClient)
            .deleteDocument(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyBoolean()
            );

        assertThatThrownBy(() -> draftPetitionRemovalService.removeDraftPetitionDocument(
            singletonList(documentWithType(DIVORCE_APPLICATION)),
            TEST_CASE_ID,
            APP_1_SOL_AUTH_TOKEN
        ))
            .hasMessageContaining("403 User role is not authorised to delete document")
            .isExactlyInstanceOf(FeignException.Forbidden.class);

        verify(idamService).retrieveUser(APP_1_SOL_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verifyNoMoreInteractions(idamService, authTokenGenerator);
    }

    @Test
    public void shouldThrow401UnAuthorizedWhenServiceAuthTokenGenerationFails() {
        List<String> solicitorRoles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");

        String solicitorRolesCsv = String.join(",", solicitorRoles);

        ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(DIVORCE_APPLICATION);

        String userId = UUID.randomUUID().toString();

        User solicitorUser = solicitorUser(solicitorRoles, userId);

        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN)).thenReturn(solicitorUser);

        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "invalidSecret",
            Response.builder()
                .request(request)
                .status(401)
                .headers(emptyMap())
                .reason("Invalid s2s secret")
                .build()
        );

        doThrow(feignException).when(authTokenGenerator).generate();

        assertThatThrownBy(() -> draftPetitionRemovalService.removeDraftPetitionDocument(
            singletonList(documentWithType(DIVORCE_APPLICATION)),
            TEST_CASE_ID,
            APP_1_SOL_AUTH_TOKEN
        ))
            .hasMessageContaining("401 Invalid s2s secret")
            .isExactlyInstanceOf(FeignException.Unauthorized.class);

        verify(idamService).retrieveUser(APP_1_SOL_AUTH_TOKEN);
        verifyNoMoreInteractions(idamService);
    }

    @Test
    public void shouldNotInvokeDocManagementWhenPetitionDocumentDoesNotExistInGenerateDocuments() {
        ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(OTHER);

        List<ListValue<DivorceDocument>> actualDocumentsList = draftPetitionRemovalService.removeDraftPetitionDocument(
            singletonList(divorceDocumentListValue),
            TEST_CASE_ID,
            APP_1_SOL_AUTH_TOKEN
        );

        assertThat(actualDocumentsList).containsExactlyInAnyOrder(divorceDocumentListValue);

        verifyNoInteractions(idamService, authTokenGenerator, documentManagementClient);
    }

    private User solicitorUser(List<String> solicitorRoles, String userId) {
        UserDetails userDetails = UserDetails
            .builder()
            .roles(solicitorRoles)
            .id(userId)
            .build();

        return new User(APP_1_SOL_AUTH_TOKEN, userDetails);
    }
}
