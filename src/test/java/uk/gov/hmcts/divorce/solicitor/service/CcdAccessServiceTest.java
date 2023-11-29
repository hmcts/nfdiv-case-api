package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_2_CITIZEN_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOL_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
public class CcdAccessServiceTest {

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CcdAccessService ccdAccessService;

    @Test
    public void shouldNotThrowAnyExceptionWhenAddApplicant1RoleIsInvoked() {
        User solicitorUser = getIdamUser(APP_1_SOL_AUTH_TOKEN, SOLICITOR_USER_ID, TEST_SOL_USER_EMAIL);
        User systemUpdateUser = getIdamUser(SYSTEM_UPDATE_AUTH_TOKEN, SYSTEM_USER_USER_ID, TEST_SYSTEM_UPDATE_USER_EMAIL);

        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN))
            .thenReturn(solicitorUser);

        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(systemUpdateUser);

        when(authTokenGenerator.generate())
            .thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(caseAssignmentApi.removeCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            )).thenReturn(CaseAssignmentUserRolesResponse.builder().build());

        when(caseAssignmentApi.addCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            )).thenReturn(CaseAssignmentUserRolesResponse.builder().build());

        assertThatCode(() -> ccdAccessService.addApplicant1SolicitorRole(APP_1_SOL_AUTH_TOKEN, TEST_CASE_ID, "1"))
            .doesNotThrowAnyException();

        verify(idamService).retrieveUser(APP_1_SOL_AUTH_TOKEN);
        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(authTokenGenerator).generate();
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            );

        verify(caseAssignmentApi)
            .addCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            );

        verifyNoMoreInteractions(idamService, authTokenGenerator, caseAssignmentApi);
    }

    @Test
    public void shouldThrowFeignUnauthorizedExceptionWhenRetrievalOfSolicitorUserFails() {
        doThrow(feignException(401, "Failed to retrieve Idam user"))
            .when(idamService).retrieveUser(APP_1_SOL_AUTH_TOKEN);

        assertThatThrownBy(() -> ccdAccessService.addApplicant1SolicitorRole(APP_1_SOL_AUTH_TOKEN, TEST_CASE_ID, null))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("Failed to retrieve Idam user");
    }

    @Test
    public void shouldThrowFeignUnauthorizedExceptionWhenRetrievalOfCaseworkerTokenFails() {
        User solicitorUser = getIdamUser(APP_1_SOL_AUTH_TOKEN, SOLICITOR_USER_ID, TEST_SOL_USER_EMAIL);

        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN))
            .thenReturn(solicitorUser);

        doThrow(feignException(401, "Failed to retrieve Idam user"))
            .when(idamService).retrieveSystemUpdateUserDetails();

        assertThatThrownBy(() -> ccdAccessService.addApplicant1SolicitorRole(APP_1_SOL_AUTH_TOKEN, TEST_CASE_ID, null))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("Failed to retrieve Idam user");

        verify(idamService).retrieveUser(APP_1_SOL_AUTH_TOKEN);

        verifyNoMoreInteractions(idamService);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionWhenServiceAuthTokenGenerationFails() {
        User solicitorUser = getIdamUser(APP_1_SOL_AUTH_TOKEN, SOLICITOR_USER_ID, TEST_SOL_USER_EMAIL);
        User systemUpdateUser = getIdamUser(SYSTEM_UPDATE_AUTH_TOKEN, SYSTEM_USER_USER_ID, TEST_SYSTEM_UPDATE_USER_EMAIL);

        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN))
            .thenReturn(solicitorUser);

        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(systemUpdateUser);

        doThrow(new InvalidTokenException("s2s secret is invalid"))
            .when(authTokenGenerator).generate();

        assertThatThrownBy(() -> ccdAccessService.addApplicant1SolicitorRole(APP_1_SOL_AUTH_TOKEN, TEST_CASE_ID, null))
            .isExactlyInstanceOf(InvalidTokenException.class)
            .hasMessageContaining("s2s secret is invalid");

        verify(idamService).retrieveUser(APP_1_SOL_AUTH_TOKEN);
        verify(idamService).retrieveSystemUpdateUserDetails();

        verifyNoMoreInteractions(idamService);
    }

    @Test
    public void shouldThrowFeignUnProcessableEntityExceptionWhenCcdClientThrowsException() {
        User solicitorUser = getIdamUser(APP_1_SOL_AUTH_TOKEN, SOLICITOR_USER_ID, TEST_SOL_USER_EMAIL);
        User systemUpdateUser = getIdamUser(SYSTEM_UPDATE_AUTH_TOKEN, SYSTEM_USER_USER_ID, TEST_SYSTEM_UPDATE_USER_EMAIL);

        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN))
            .thenReturn(solicitorUser);

        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(systemUpdateUser);

        when(authTokenGenerator.generate())
            .thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(caseAssignmentApi.removeCaseUserRoles(
            eq(SYSTEM_UPDATE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            any(CaseAssignmentUserRolesRequest.class)
        )).thenThrow(feignException(422, "Case roles not valid"));

        assertThatThrownBy(() -> ccdAccessService.addApplicant1SolicitorRole(APP_1_SOL_AUTH_TOKEN, TEST_CASE_ID, null))
            .isExactlyInstanceOf(FeignException.UnprocessableEntity.class)
            .hasMessageContaining("Case roles not valid");

        verify(idamService).retrieveUser(APP_1_SOL_AUTH_TOKEN);
        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(authTokenGenerator).generate();

        verifyNoMoreInteractions(idamService, authTokenGenerator);
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenLinkApplicant2ToApplicationIsInvoked() {
        User systemUpdateUser = getIdamUser(SYSTEM_UPDATE_AUTH_TOKEN, CASEWORKER_USER_ID, TEST_CASEWORKER_USER_EMAIL);

        when(idamService.retrieveUser(SYSTEM_UPDATE_AUTH_TOKEN))
            .thenReturn(systemUpdateUser);

        when(authTokenGenerator.generate())
            .thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(caseAssignmentApi.addCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            )
        ).thenReturn(any());

        assertThatCode(() -> ccdAccessService.linkRespondentToApplication(SYSTEM_UPDATE_AUTH_TOKEN, TEST_CASE_ID, APP_2_CITIZEN_USER_ID))
            .doesNotThrowAnyException();

        verify(idamService).retrieveUser(SYSTEM_UPDATE_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            );

        verifyNoMoreInteractions(idamService, authTokenGenerator, caseAssignmentApi);
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenUnLinkApp2FromApplicationIsInvoked() {
        User systemUpdateUser = getIdamUser(SYSTEM_UPDATE_AUTH_TOKEN, CASEWORKER_USER_ID, TEST_CASEWORKER_USER_EMAIL);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUpdateUser);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        assertThatCode(() -> ccdAccessService.unlinkApplicant2FromCase(TEST_CASE_ID, APP_2_CITIZEN_USER_ID))
            .doesNotThrowAnyException();

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(authTokenGenerator).generate();
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            );

        verifyNoMoreInteractions(idamService, authTokenGenerator, caseAssignmentApi);
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenUnLinkUserFromApplicationIsInvoked() {
        User systemUpdateUser = getIdamUser(SYSTEM_UPDATE_AUTH_TOKEN, CASEWORKER_USER_ID, TEST_CASEWORKER_USER_EMAIL);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUpdateUser);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(caseAssignmentApi.removeCaseUserRoles(
            eq(SYSTEM_UPDATE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            any(CaseAssignmentUserRolesRequest.class)
        )).thenReturn(CaseAssignmentUserRolesResponse.builder().build());

        assertThatCode(() -> ccdAccessService.unlinkUserFromCase(TEST_CASE_ID, SOLICITOR_USER_ID))
            .doesNotThrowAnyException();

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(authTokenGenerator).generate();
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            );

        verifyNoMoreInteractions(idamService, authTokenGenerator, caseAssignmentApi);
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenRemoveRolesIsCalled() {
        var systemUpdateUser = getIdamUser(SYSTEM_UPDATE_AUTH_TOKEN, CASEWORKER_USER_ID, TEST_CASEWORKER_USER_EMAIL);

        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(systemUpdateUser);

        when(authTokenGenerator.generate())
            .thenReturn(TEST_SERVICE_AUTH_TOKEN);

        var response = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("NOT_THIS_ONE").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();

        when(caseAssignmentApi.getUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                anyList()
            )
        ).thenReturn(response);

        when(caseAssignmentApi.removeCaseUserRoles(
            eq(SYSTEM_UPDATE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            any(CaseAssignmentUserRolesRequest.class)
        )).thenReturn(CaseAssignmentUserRolesResponse.builder().build());

        assertThatCode(() -> ccdAccessService.removeUsersWithRole(TEST_CASE_ID, List.of("[CREATOR]")))
            .doesNotThrowAnyException();

        var request = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(
                CaseAssignmentUserRoleWithOrganisation.builder()
                    .organisationId(null)
                    .caseDataId("1616591401473378")
                    .caseRole("[CREATOR]")
                    .userId("2")
                    .build()))
            .build();

        verify(idamService, times(2)).retrieveSystemUpdateUserDetails();
        verify(authTokenGenerator).generate();
        verify(caseAssignmentApi)
            .getUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                anyList()
            );

        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                eq(request)
            );

        verifyNoMoreInteractions(idamService, authTokenGenerator, caseAssignmentApi);
    }

    @Test
    public void shouldReturnTrueWhenUserHasCreatorRole() {
        User user = new User(TEST_SERVICE_AUTH_TOKEN, UserInfo.builder().uid("user-id").build());
        when(idamService.retrieveUser(SYSTEM_UPDATE_AUTH_TOKEN)).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(
            SYSTEM_UPDATE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            List.of(TEST_CASE_ID.toString()),
            List.of("user-id")
        )).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().caseRole(CREATOR.getRole()).build()
            )).build()
        );

        boolean expected = ccdAccessService.isApplicant1(SYSTEM_UPDATE_AUTH_TOKEN, TEST_CASE_ID);

        assertThat(expected).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenUserHasApplicant2Role() {
        User user = new User(TEST_SERVICE_AUTH_TOKEN, UserInfo.builder().uid("user-id").build());
        when(idamService.retrieveUser(SYSTEM_UPDATE_AUTH_TOKEN)).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(
            SYSTEM_UPDATE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            List.of(TEST_CASE_ID.toString()),
            List.of("user-id")
        )).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().caseRole(APPLICANT_2.getRole()).build()
            )).build()
        );

        boolean expected = ccdAccessService.isApplicant2(SYSTEM_UPDATE_AUTH_TOKEN, TEST_CASE_ID);

        assertThat(expected).isTrue();
    }

    @Test
    public void shouldReturnCaseAssignmentUserRoles() {
        List<CaseAssignmentUserRole> expectedRoles = List.of(
            CaseAssignmentUserRole.builder().caseRole("[APPSOLICITORONE]")
                .userId("2").build(),
            CaseAssignmentUserRole.builder().caseRole("[APPSOLICITORTWO]")
                .userId("4").build()
        );
        Long caseId = 123456L;
        User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().uid("id").sub("email").build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(caseAssignmentApi.getUserRoles(SYSTEM_UPDATE_AUTH_TOKEN, SERVICE_AUTHORIZATION, List.of(caseId.toString())))
            .thenReturn(CaseAssignmentUserRolesResource.builder()
                .caseAssignmentUserRoles(expectedRoles)
                .build());

        List<CaseAssignmentUserRole> actualRoles = ccdAccessService.getCaseAssignmentUserRoles(caseId);

        assertThat(actualRoles).isEqualTo(expectedRoles);
    }

    private User getIdamUser(String authToken, String userId, String email) {
        return new User(
            authToken,
            UserInfo.builder().uid(userId).sub(email).build()
        );
    }
}
