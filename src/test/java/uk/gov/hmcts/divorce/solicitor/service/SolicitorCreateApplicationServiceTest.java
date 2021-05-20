package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.service.updater.ClaimsCost;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorCourtDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationContactInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateApplicationServiceTest {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);

    @Mock
    private ClaimsCost claimsCost;

    @Mock
    private SolicitorCourtDetails solicitorCourtDetails;

    @Mock
    private MiniApplicationDraft miniApplicationDraft;

    @Mock
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Mock
    private OrganisationClient organisationClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Test
    void shouldCompleteStepsToCreateApplication() {
        final CaseData caseData = mock(CaseData.class);
        final CaseDataUpdaterChain caseDataUpdaterChain = mock(CaseDataUpdaterChain.class);

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            claimsCost,
            solicitorCourtDetails,
            miniApplicationDraft);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(caseDataUpdaterChainFactory.createWith(caseDataUpdaters)).thenReturn(caseDataUpdaterChain);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseData actualCaseData = solicitorCreateApplicationService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(actualCaseData).isEqualTo(caseData);

        verify(caseDataUpdaterChainFactory).createWith(caseDataUpdaters);
        verify(caseDataUpdaterChain).processNext(caseDataContext);

        verifyNoMoreInteractions(caseDataUpdaterChainFactory, caseDataUpdaterChain);
    }

    @Test
    public void shouldValidateApplicant1SolicitorOrgAndReturnNoErrorsWhenSolicitorBelongsToSelectedOrg() {
        CaseData caseData = caseData();
        caseData.setApplicant1OrganisationPolicy(organisationPolicy());

        OrganisationsResponse organisationsResponse = OrganisationsResponse
            .builder()
            .organisationIdentifier(TEST_ORG_ID)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(organisationClient.getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(organisationsResponse);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            solicitorCreateApplicationService.validateSolicitorOrganisation(
                caseData,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN
            );

        assertThat(aboutToStartOrSubmitResponse.getData()).isEqualTo(caseData);
        assertThat(aboutToStartOrSubmitResponse.getErrors()).isNull();
        assertThat(aboutToStartOrSubmitResponse.getWarnings()).isNull();

        verify(authTokenGenerator).generate();
        verify(organisationClient).getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(authTokenGenerator, organisationClient);
    }

    @Test
    public void shouldValidateApplicant1SolicitorOrgAndReturnErrorWhenSolicitorDoesNotBelongsToSelectedOrg() {
        CaseData caseData = caseData();
        caseData.setApplicant1OrganisationPolicy(organisationPolicy()); // default org identifier = ABC123

        OrganisationsResponse organisationsResponse = OrganisationsResponse
            .builder()
            .organisationIdentifier("TESTORG123")
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(organisationClient.getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(organisationsResponse);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            solicitorCreateApplicationService.validateSolicitorOrganisation(
                caseData,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN
            );

        assertThat(aboutToStartOrSubmitResponse.getData()).isEqualTo(caseData);
        assertThat(aboutToStartOrSubmitResponse.getErrors()).containsExactly("Please select an organisation you belong to");
        assertThat(aboutToStartOrSubmitResponse.getWarnings()).isNull();

        verify(authTokenGenerator).generate();
        verify(organisationClient).getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(authTokenGenerator, organisationClient);
    }

    @Test
    public void shouldValidateApplicant1SolicitorOrgAndReturnErrorWhenSolicitorOrgIsNotPopulated() {
        CaseData caseData = caseData();

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            solicitorCreateApplicationService.validateSolicitorOrganisation(
                caseData,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN
            );

        assertThat(aboutToStartOrSubmitResponse.getData()).isEqualTo(caseData);
        assertThat(aboutToStartOrSubmitResponse.getErrors()).containsExactly("Please select an organisation");
        assertThat(aboutToStartOrSubmitResponse.getWarnings()).isNull();

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
        caseData.setApplicant1OrganisationPolicy(organisationPolicy());

        assertThatThrownBy(() -> solicitorCreateApplicationService.validateSolicitorOrganisation(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN
        ))
            .hasMessageContaining("403 Service not whitelisted")
            .isExactlyInstanceOf(FeignException.Forbidden.class);
    }

    @Test
    void shouldSetApplicant2OrgContactInformationWhenApplicant2IsRepresentedBySolicitorAndIsDigital() {
        CaseData caseData = caseData();
        caseData.setApplicant1OrganisationPolicy(organisationPolicy());
        caseData.setApplicant2OrganisationPolicy(organisationPolicy());
        caseData.setApp2SolDigital(YES);
        caseData.setApplicant2SolicitorRepresented(YES);

        OrganisationsResponse organisationsResponse = OrganisationsResponse
            .builder()
            .organisationIdentifier(TEST_ORG_ID)
            .contactInformation(singletonList(organisationContactInformation()))
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(organisationClient.getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(organisationsResponse);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            solicitorCreateApplicationService.setApplicant2SolOrganisationInfo(
                caseData,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN
            );

        CaseData expectedCaseData = caseData.toBuilder().build();
        expectedCaseData.setApplicant2OrgContactInformation(organisationContactInformation());

        assertThat(aboutToStartOrSubmitResponse.getData()).isEqualTo(expectedCaseData);
        assertThat(aboutToStartOrSubmitResponse.getErrors()).isNull();
        assertThat(aboutToStartOrSubmitResponse.getWarnings()).isNull();

        verify(authTokenGenerator).generate();
        verify(organisationClient).getUserOrganisation(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(authTokenGenerator, organisationClient);
    }

    @Test
    public void shouldNotSetApplicant2OrgContactInformationWhenApplicant2IsRepresentedBySolicitorAndIsNotDigital() {
        CaseData caseData = caseData();
        caseData.setApplicant1OrganisationPolicy(organisationPolicy());
        caseData.setApp2SolDigital(NO);
        caseData.setApplicant2SolicitorRepresented(YES);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            solicitorCreateApplicationService.setApplicant2SolOrganisationInfo(
                caseData,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN
            );

        assertThat(aboutToStartOrSubmitResponse.getData()).isEqualTo(caseData);
        assertThat(aboutToStartOrSubmitResponse.getErrors()).isNull();
        assertThat(aboutToStartOrSubmitResponse.getWarnings()).isNull();
    }

    @Test
    public void shouldNotSetApplicant2OrgContactInformationWhenApplicant2IsNotRepresentedBySolicitor() {
        CaseData caseData = caseData();
        caseData.setApplicant1OrganisationPolicy(organisationPolicy());
        caseData.setApp2SolDigital(NO);
        caseData.setApplicant2SolicitorRepresented(NO);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            solicitorCreateApplicationService.setApplicant2SolOrganisationInfo(
                caseData,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN
            );

        assertThat(aboutToStartOrSubmitResponse.getData()).isEqualTo(caseData);
        assertThat(aboutToStartOrSubmitResponse.getErrors()).isNull();
        assertThat(aboutToStartOrSubmitResponse.getWarnings()).isNull();
    }
}
