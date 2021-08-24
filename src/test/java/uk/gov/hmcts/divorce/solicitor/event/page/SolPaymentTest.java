package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.client.pba.OrganisationEntityResponse;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaRefDataClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolPaymentTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private IdamService idamService;

    @Mock
    private PbaRefDataClient pbaRefDataClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SolPayment solPayment;

    @Mock
    private ResponseEntity<PbaOrganisationResponse> responseEntity;

    @Test
    public void shouldRetrieveAndSetPbaNumbersWhenPaymentMethodIsPba() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        UserDetails solUserDetails = UserDetails.builder().email(TEST_SOLICITOR_EMAIL).build();

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

        AboutToStartOrSubmitResponse<CaseData, State> response = solPayment.midEvent(details, details);

        DynamicList pbaNumbers = response.getData().getApplication().getPbaNumbers();

        assertThat(pbaNumbers).isNotNull();
        assertThat(pbaNumbers.getListItems()).extracting("label").containsExactlyInAnyOrder("PBA0012345", "PBA0012346");
    }
}
