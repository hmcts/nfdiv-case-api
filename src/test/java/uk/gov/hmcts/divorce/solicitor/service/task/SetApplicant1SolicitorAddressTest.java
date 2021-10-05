package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class SetApplicant1SolicitorAddressTest {

    @Mock
    private OrganisationClient organisationClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SetApplicant1SolicitorAddress setApplicant1SolicitorAddress;

    @Test
    void shouldSetApplicant1SolicitorAddressFromOrganisationPolicyIfApplicant1IsRepresented() {

        final var caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final var organisationsResponse = mock(OrganisationsResponse.class);
        final var organisationContactInformation = OrganisationContactInformation.builder()
            .addressLine1("Line 1")
            .addressLine2("Line 2")
            .addressLine3("")
            .townCity("Town")
            .county(null)
            .postCode("Post Code")
            .country("Country")
            .build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTH_HEADER_VALUE);
        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(organisationClient.getUserOrganisation(AUTH_HEADER_VALUE, TEST_AUTHORIZATION_TOKEN))
            .thenReturn(organisationsResponse);
        when(organisationsResponse.getContactInformation())
            .thenReturn(singletonList(organisationContactInformation));

        final CaseDetails<CaseData, State> result = setApplicant1SolicitorAddress.apply(caseDetails);

        assertThat(result.getData().getApplicant1().getSolicitor().getAddress())
            .isEqualTo("Line 1\nLine 2\nTown\nPost Code\nCountry");
    }

    @Test
    void shouldNotSetAddressIfApplicant1IsNotRepresented() {

        final var caseData = CaseData.builder()
            .applicant1(getApplicant())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setApplicant1SolicitorAddress.apply(caseDetails);

        assertThat(result.getData().getApplicant1().getSolicitor()).isNull();
    }

    @Test
    void shouldNotSetAddressIfEmptyListOfContactInformation() {

        final var caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final var organisationsResponse = mock(OrganisationsResponse.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTH_HEADER_VALUE);
        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(organisationClient.getUserOrganisation(AUTH_HEADER_VALUE, TEST_AUTHORIZATION_TOKEN))
            .thenReturn(organisationsResponse);
        when(organisationsResponse.getContactInformation())
            .thenReturn(emptyList());

        final CaseDetails<CaseData, State> result = setApplicant1SolicitorAddress.apply(caseDetails);

        assertThat(result.getData().getApplicant1().getSolicitor().getAddress()).isNull();
    }

    @Test
    void shouldNotSetAddressIfNullListOfContactInformation() {

        final var caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final var organisationsResponse = mock(OrganisationsResponse.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTH_HEADER_VALUE);
        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(organisationClient.getUserOrganisation(AUTH_HEADER_VALUE, TEST_AUTHORIZATION_TOKEN))
            .thenReturn(organisationsResponse);
        when(organisationsResponse.getContactInformation())
            .thenReturn(null);

        final CaseDetails<CaseData, State> result = setApplicant1SolicitorAddress.apply(caseDetails);

        assertThat(result.getData().getApplicant1().getSolicitor().getAddress()).isNull();
    }
}