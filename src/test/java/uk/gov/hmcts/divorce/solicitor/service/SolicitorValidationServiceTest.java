package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationUser;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitorValidationServiceTest {

    private static final String AUTH_TOKEN = "auth";
    private static final String S2S_TOKEN = "s2s";
    private static final String SOL_EMAIL = "sol@gmail.com";
    private static final String USER_ID = "userId";
    private static final String ORG_ID = "orgId";

    @Mock
    private CcdAccessService ccdAccessService;
    @Mock
    private OrganisationClient organisationClient;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SolicitorValidationService solicitorValidationService;

    @Test
    void shouldReturnUserIdWhenUserWithEmailExists() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(organisationClient.findUserByEmail(AUTH_TOKEN, S2S_TOKEN, SOL_EMAIL)).thenReturn(new OrganisationUser(USER_ID));

        Optional<String> userIdOption = solicitorValidationService.findSolicitorByEmail(SOL_EMAIL, null);

        assertThat(userIdOption).isPresent();
        assertThat(userIdOption.get()).isEqualTo(USER_ID);
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserWithEmailNotExists() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(organisationClient.findUserByEmail(AUTH_TOKEN, S2S_TOKEN, SOL_EMAIL)).thenThrow(FeignException.NotFound.class);

        Optional<String> userIdOption = solicitorValidationService.findSolicitorByEmail(SOL_EMAIL, null);

        assertThat(userIdOption).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenUserIsInOrganisation() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);

        FindUsersByOrganisationResponse expectedResponse = FindUsersByOrganisationResponse.builder()
            .users(List.of(ProfessionalUser.builder()
                    .userIdentifier(USER_ID)
                .build(),
                ProfessionalUser.builder()
                    .userIdentifier("SOME_OTHER_ID")
                    .build()))
            .build();

        when(organisationClient.getOrganisationUsers(AUTH_TOKEN, S2S_TOKEN, ORG_ID)).thenReturn(expectedResponse);

        boolean result = solicitorValidationService.isSolicitorInOrganisation(USER_ID, ORG_ID);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIsNotInOrganisation() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);

        FindUsersByOrganisationResponse expectedResponse = FindUsersByOrganisationResponse.builder()
            .users(List.of(ProfessionalUser.builder()
                    .userIdentifier("RANDOM_USER")
                    .build(),
                ProfessionalUser.builder()
                    .userIdentifier("SOME_OTHER_ID")
                    .build()))
            .build();

        when(organisationClient.getOrganisationUsers(AUTH_TOKEN, S2S_TOKEN, ORG_ID)).thenReturn(expectedResponse);

        boolean result = solicitorValidationService.isSolicitorInOrganisation(USER_ID, ORG_ID);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldReturnErrorWhenUserNotExists() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(organisationClient.findUserByEmail(AUTH_TOKEN, S2S_TOKEN, SOL_EMAIL)).thenThrow(FeignException.NotFound.class);

        List<String> errors = solicitorValidationService.validateEmailBelongsToOrgUser(SOL_EMAIL, null, ORG_ID);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo("No user found with provided email. Please check the email address and try again");
    }

    @Test
    public void shouldReturnErrorWhenUserNotInOrg() {
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(organisationClient.findUserByEmail(AUTH_TOKEN, S2S_TOKEN, SOL_EMAIL)).thenReturn(new OrganisationUser(USER_ID));
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(AUTH_TOKEN, null));

        FindUsersByOrganisationResponse expectedResponse = FindUsersByOrganisationResponse.builder()
            .users(List.of(ProfessionalUser.builder()
                    .userIdentifier("RANDOM_USER")
                    .build(),
                ProfessionalUser.builder()
                    .userIdentifier("SOME_OTHER_ID")
                    .build()))
            .build();

        when(organisationClient.getOrganisationUsers(AUTH_TOKEN, S2S_TOKEN, ORG_ID)).thenReturn(expectedResponse);

        List<String> errors = solicitorValidationService.validateEmailBelongsToOrgUser(SOL_EMAIL, null, ORG_ID);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(
            "The email address provided does not belong to a user in the selected organisation. Please ensure the user is in the "
            + "selected organisation.");
    }

    @Test
    public void shouldNotReturnErrorWhenValidUser() {
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(organisationClient.findUserByEmail(AUTH_TOKEN, S2S_TOKEN, SOL_EMAIL)).thenReturn(new OrganisationUser(USER_ID));
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(AUTH_TOKEN, null));

        FindUsersByOrganisationResponse expectedResponse = FindUsersByOrganisationResponse.builder()
            .users(List.of(ProfessionalUser.builder()
                    .userIdentifier(USER_ID)
                    .build(),
                ProfessionalUser.builder()
                    .userIdentifier("SOME_OTHER_ID")
                    .build()))
            .build();

        when(organisationClient.getOrganisationUsers(AUTH_TOKEN, S2S_TOKEN, ORG_ID)).thenReturn(expectedResponse);

        List<String> errors = solicitorValidationService.validateEmailBelongsToOrgUser(SOL_EMAIL, null, ORG_ID);

        assertThat(errors).hasSize(0);
    }
}
