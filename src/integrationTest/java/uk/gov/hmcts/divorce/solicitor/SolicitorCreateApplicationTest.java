package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreateApplication.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssembly;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stubGetOrganisationEndpoint;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stubGetOrganisationEndpointForFailure;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_THE_SOL_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_MID_EVENT_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_MID_EVENT_RESPONSE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    PrdOrganisationWireMock.PropertiesInitializer.class,
    DocAssemblyWireMock.PropertiesInitializer.class})
class SolicitorCreateApplicationTest {

    private static final String SOLICITOR_CREATE_ABOUT_TO_SUBMIT = "classpath:solicitor-create-about-to-submit-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private DocumentIdProvider documentIdProvider;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        PrdOrganisationWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        PrdOrganisationWireMock.stopAndReset();
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Divorce application");

        stubForDocAssembly();

        final var jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithApplicant1AndApplicant2Org(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse(SOLICITOR_CREATE_ABOUT_TO_SUBMIT));
    }

    @Test
    void shouldValidateApplicant1SolicitorOrgAndReturnNoErrorsWhenSolicitorBelongsToSelectedOrg() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint(getOrganisationResponseWith(TEST_ORG_ID));

        final var jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_THE_SOL_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithApplicant1Org(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse(SOLICITOR_MID_EVENT_RESPONSE));
    }

    @Test
    public void shouldValidateApplicant1SolicitorOrgAndReturnErrorWhenSolicitorDoesNotBelongsToSelectedOrg() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint(getOrganisationResponseWith("TESTORG123"));

        final var jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_THE_SOL_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithApplicant1Org(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse(SOLICITOR_MID_EVENT_ERROR));
    }

    @Test
    public void shouldThrow403ForbiddenExceptionWhenServiceIsNotWhitelistedInReferenceData() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpointForFailure();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_THE_SOL_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithApplicant1Org(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isForbidden()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Forbidden.class)
            );
    }

    private String getOrganisationResponseWith(final String organisationId) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            OrganisationsResponse.builder()
                .organisationIdentifier(organisationId)
                .build());
    }

    private CaseData caseDataWithApplicant1Org() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(Solicitor.builder().organisationPolicy(organisationPolicy()).build());
        return caseData;
    }

    private static CaseData caseDataWithApplicant1AndApplicant2Org() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(Solicitor.builder().organisationPolicy(organisationPolicy()).build());
        caseData.getApplicant2().setSolicitor(Solicitor.builder().organisationPolicy(organisationPolicy()).build());
        return caseData;
    }

    public static Applicant getApplicant() {
        return Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .languagePreferenceWelsh(NO)
            .homeAddress(AddressGlobalUK.builder()
                .addressLine1("line 1")
                .postTown("town")
                .postCode("postcode")
                .build())
            .build();
    }

    private static CaseData caseData() {
        var applicant1 = getApplicant();
        applicant1.setFinancialOrder(NO);

        return CaseData
            .builder()
            .applicant1(applicant1)
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();
    }
}
