package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_2_CITIZEN_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
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
    @InjectMocks
    private CcdAccessService ccdAccessService;

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

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
    public void shouldNotThrowAnyExceptionWhenUnLinkUserFromApplicationIsInvoked() {
        User systemUpdateUser = getIdamUser(SYSTEM_UPDATE_AUTH_TOKEN, CASEWORKER_USER_ID, TEST_CASEWORKER_USER_EMAIL);

        when(idamService.retrieveUser(SYSTEM_UPDATE_AUTH_TOKEN))
            .thenReturn(systemUpdateUser);

        when(authTokenGenerator.generate())
            .thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(caseAssignmentApi.removeCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            )
        ).thenReturn(any());

        assertThatCode(() -> ccdAccessService.unlinkUserFromApplication(SYSTEM_UPDATE_AUTH_TOKEN, TEST_CASE_ID, APP_2_CITIZEN_USER_ID))
            .doesNotThrowAnyException();

        verify(idamService).retrieveUser(SYSTEM_UPDATE_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                eq(SYSTEM_UPDATE_AUTH_TOKEN),
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(CaseAssignmentUserRolesRequest.class)
            );

        verifyNoMoreInteractions(idamService, authTokenGenerator, caseAssignmentApi);
    }

    private User getIdamUser(String authToken, String userId, String email) {
        return new User(
            authToken,
            UserDetails.builder().id(userId).email(email).build()
        );
    }
}
