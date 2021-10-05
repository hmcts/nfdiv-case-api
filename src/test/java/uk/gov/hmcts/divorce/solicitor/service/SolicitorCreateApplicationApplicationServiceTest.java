package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.service.task.DivorceApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.task.InitialiseSolicitorCreatedApplication;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicant1SolicitorAddress;
import uk.gov.hmcts.divorce.solicitor.service.task.SolicitorCourtDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateApplicationApplicationServiceTest {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);

    @Mock
    private InitialiseSolicitorCreatedApplication initialiseSolicitorCreatedApplication;

    @Mock
    private SolicitorCourtDetails solicitorCourtDetails;

    @Mock
    private DivorceApplicationDraft divorceApplicationDraft;

    @Mock
    private SetApplicant1SolicitorAddress setApplicant1SolicitorAddress;

    @Mock
    private OrganisationClient organisationClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Test
    void shouldCompleteStepsToCreateApplication() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(initialiseSolicitorCreatedApplication.apply(caseDetails)).thenReturn(caseDetails);
        when(solicitorCourtDetails.apply(caseDetails)).thenReturn(caseDetails);
        when(setApplicant1SolicitorAddress.apply(caseDetails)).thenReturn(caseDetails);
        when(divorceApplicationDraft.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> result = solicitorCreateApplicationService.aboutToSubmit(caseDetails);

        assertThat(result.getData()).isEqualTo(caseData);

        verify(initialiseSolicitorCreatedApplication).apply(caseDetails);
        verify(solicitorCourtDetails).apply(caseDetails);
        verify(divorceApplicationDraft).apply(caseDetails);
    }

    @Test
    public void shouldValidateApplicant1SolicitorOrgAndReturnNoErrorsWhenSolicitorBelongsToSelectedOrg() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder().organisationPolicy(organisationPolicy()).build()
        );

        OrganisationsResponse organisationsResponse = OrganisationsResponse
            .builder()
            .organisationIdentifier(TEST_ORG_ID)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(organisationClient.getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(organisationsResponse);

        final CaseInfo caseInfo = solicitorCreateApplicationService.validateSolicitorOrganisation(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(caseInfo.getCaseData()).isNull();
        assertThat(caseInfo.getErrors()).isNull();

        verify(authTokenGenerator).generate();
        verify(organisationClient).getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(authTokenGenerator, organisationClient);
    }

    @Test
    public void shouldValidateApplicant1SolicitorOrgAndReturnErrorWhenSolicitorDoesNotBelongsToSelectedOrg() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder().organisationPolicy(organisationPolicy()).build()
        );

        OrganisationsResponse organisationsResponse = OrganisationsResponse
            .builder()
            .organisationIdentifier("TESTORG123")
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(organisationClient.getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(organisationsResponse);

        final CaseInfo caseInfo = solicitorCreateApplicationService.validateSolicitorOrganisation(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(caseInfo.getCaseData()).isNull();
        assertThat(caseInfo.getErrors()).containsExactly("Please select an organisation you belong to");

        verify(authTokenGenerator).generate();
        verify(organisationClient).getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(authTokenGenerator, organisationClient);
    }

    @Test
    public void shouldValidateApplicant1SolicitorOrgAndReturnErrorWhenSolicitorOrgIsNotPopulated() {
        CaseData caseData = caseData();

        final CaseInfo caseInfo = solicitorCreateApplicationService.validateSolicitorOrganisation(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(caseInfo.getCaseData()).isNull();
        assertThat(caseInfo.getErrors()).containsExactly("Please select an organisation");

        verifyNoMoreInteractions(authTokenGenerator, organisationClient);
    }

    @Test
    public void shouldThrow403ForbiddenExceptionWhenServiceIsNotWhitelistedInReferenceData() {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "serviceNotWhitelisted",
            Response.builder()
                .request(request)
                .status(403)
                .headers(Collections.emptyMap())
                .reason("Service not whitelisted")
                .build()
        );

        doThrow(feignException)
            .when(organisationClient)
            .getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder().organisationPolicy(organisationPolicy()).build()
        );

        assertThatThrownBy(() -> solicitorCreateApplicationService.validateSolicitorOrganisation(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN
        ))
            .hasMessageContaining("403 Service not whitelisted")
            .isExactlyInstanceOf(FeignException.Forbidden.class);
    }
}
