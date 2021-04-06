package uk.gov.hmcts.reform.divorce.caseapi.service;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.INVALID_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.PET_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_CASEWORKER_USER_PASSWORD;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_SOL_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.caseapi.util.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
public class IdamServiceTest {

    @InjectMocks
    private IdamService idamService;

    @Mock
    private IdamClient idamClient;

    @Test
    public void shouldRetrieveUserWhenValidAuthorizationTokenIsPassed() {
        when(idamClient.getUserDetails(PET_SOL_AUTH_TOKEN))
            .thenReturn(userDetails());

        assertThatCode(() -> idamService.retrieveUser(PET_SOL_AUTH_TOKEN))
            .doesNotThrowAnyException();

        verify(idamClient).getUserDetails(PET_SOL_AUTH_TOKEN);
        verifyNoMoreInteractions(idamClient);
    }

    @Test
    public void shouldThrowFeignUnauthorizedExceptionWhenInValidAuthorizationTokenIsPassed() {
        doThrow(feignException(401, "Failed to retrieve Idam user"))
            .when(idamClient).getUserDetails("Bearer invalid_token");

        assertThatThrownBy(() -> idamService.retrieveUser(INVALID_AUTH_TOKEN))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("Failed to retrieve Idam user");
    }

    @Test
    public void shouldNotThrowExceptionAndRetrieveCaseworkerUserSuccessfully() {
        setCaseworkerCredentials();

        when(idamClient.getAccessToken(TEST_CASEWORKER_USER_EMAIL, TEST_CASEWORKER_USER_PASSWORD))
            .thenReturn(PET_SOL_AUTH_TOKEN);

        when(idamClient.getUserDetails(PET_SOL_AUTH_TOKEN))
            .thenReturn(userDetails());

        assertThatCode(() -> idamService.retrieveCaseWorkerDetails())
            .doesNotThrowAnyException();

        verify(idamClient).getAccessToken(TEST_CASEWORKER_USER_EMAIL, TEST_CASEWORKER_USER_PASSWORD);
        verify(idamClient).getUserDetails(PET_SOL_AUTH_TOKEN);
        verifyNoMoreInteractions(idamClient);
    }

    @Test
    public void shouldThrowFeignUnauthorizedExceptionWhenCaseworkerCredentialsAreInvalid() {
        setCaseworkerCredentials();

        doThrow(feignException(401, "Failed to retrieve Idam user"))
            .when(idamClient).getAccessToken(TEST_CASEWORKER_USER_EMAIL, TEST_CASEWORKER_USER_PASSWORD);

        assertThatThrownBy(() -> idamService.retrieveCaseWorkerDetails())
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("Failed to retrieve Idam user");
    }

    private void setCaseworkerCredentials() {
        ReflectionTestUtils.setField(idamService, "caseworkerUserName", TEST_CASEWORKER_USER_EMAIL);
        ReflectionTestUtils.setField(idamService, "caseworkerPassword", TEST_CASEWORKER_USER_PASSWORD);
    }

    private UserDetails userDetails() {
        return UserDetails
            .builder()
            .id(SOLICITOR_USER_ID)
            .email(TEST_SOL_USER_EMAIL)
            .build();
    }
}
