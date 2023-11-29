package uk.gov.hmcts.divorce.solicitor.client.pba;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;

@ExtendWith(MockitoExtension.class)
public class PbaServiceTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private IdamService idamService;

    @Mock
    private PbaRefDataClient pbaRefDataClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private PbaService pbaService;

    @Mock
    private ResponseEntity<PbaOrganisationResponse> responseEntity;

    @Test
    void shouldPopulatePbaDynamicList() {

        var solUserDetails = UserInfo.builder().sub(TEST_SOLICITOR_EMAIL).build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(new User(TEST_AUTHORIZATION_TOKEN, solUserDetails));

        when(pbaRefDataClient.retrievePbaNumbers(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_SOLICITOR_EMAIL))
            .thenReturn(responseEntity);

        var pbaOrganisationResponse = PbaOrganisationResponse
            .builder()
            .organisationEntityResponse(
                OrganisationEntityResponse
                    .builder()
                    .paymentAccount(List.of("PBA0012345", "PBA0012346"))
                    .build()
            )
            .build();

        when(responseEntity.getBody()).thenReturn(pbaOrganisationResponse);

        DynamicList pbaNumbers = pbaService.populatePbaDynamicList();

        assertThat(pbaNumbers).isNotNull();
        assertThat(pbaNumbers.getListItems()).extracting("label").containsExactlyInAnyOrder("PBA0012345", "PBA0012346");
    }
}
