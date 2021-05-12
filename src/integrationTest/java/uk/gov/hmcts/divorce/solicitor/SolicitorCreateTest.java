package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import feign.FeignException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.DocAssemblyService;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreatePetitionService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_COSTS_CLAIM;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreate.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.divorce.testutil.TestConstants.MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.PETITIONER_ORGANISATION_POLICY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {SolicitorCreateTest.PropertiesInitializer.class})
class SolicitorCreateTest {

    private static final String SOLICITOR_CREATE_ABOUT_TO_SUBMIT = "classpath:solicitor-create-about-to-submit-response.json";
    private static final String SOLICITOR_CREATE_MID_EVENT = "classpath:solicitor-create-mid-event-response.json";
    private static final String SOLICITOR_CREATE_MID_EVENT_ERROR = "classpath:solicitor-create-mid-event-error-response.json";

    private static final String SERVICE_AUTH_TOKEN = "test-service-auth-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @Autowired
    private DocAssemblyService docAssemblyService;

    @Autowired
    private DocmosisTemplatesConfig docmosisTemplatesConfig;

    @Autowired
    private SolicitorCreatePetitionService solicitorCreatePetitionService;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    private static final WireMockServer DOC_ASSEMBLY_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private static final WireMockServer PRD_ORGANISATION_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeAll
    static void setUp() {
        DOC_ASSEMBLY_SERVER.start();
        PRD_ORGANISATION_SERVER.start();
    }

    @AfterAll
    static void tearDown() {
        DOC_ASSEMBLY_SERVER.stop();
        DOC_ASSEMBLY_SERVER.resetAll();

        PRD_ORGANISATION_SERVER.stop();
        PRD_ORGANISATION_SERVER.resetAll();
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly()
        throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        stubForDocAssembly();

        final String jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_CREATE_ABOUT_TO_SUBMIT), STRICT);
    }

    @Test
    void shouldValidatePetitionerSolicitorOrgAndReturnNoErrorsWhenSolicitorBelongsToSelectedOrg() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint(TEST_ORG_ID);

        final String jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithPetitionerOrg(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_CREATE_MID_EVENT), STRICT);
    }

    @Test
    public void shouldValidatePetitionerSolicitorOrgAndReturnErrorWhenSolicitorDoesNotBelongsToSelectedOrg() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint("TESTORG123");

        final String jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithPetitionerOrg(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_CREATE_MID_EVENT_ERROR), STRICT);
    }

    @Test
    public void shouldThrow403ForbiddenExceptionWhenServiceIsNotWhitelistedInReferenceData() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpointForFailure();

        mockMvc.perform(MockMvcRequestBuilders.post(MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithPetitionerOrg(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isForbidden()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Forbidden.class)
            );
    }


    private String expectedResponse(final String jsonFile) throws IOException {
        final File issueFeesResponseJsonFile = getFile(jsonFile);

        return new String(readAllBytes(issueFeesResponseJsonFile.toPath()));
    }

    private Map<String, Object> caseData() {
        Map<String, Object> caseData = caseDataMap();
        return caseData;
    }

    private Map<String, Object> caseDataWithPetitionerOrg() {
        Map<String, Object> caseData = caseDataMap();
        caseData.put(PETITIONER_ORGANISATION_POLICY, (organisationPolicy()));

        return caseData;
    }

    private Map<String, Object> caseDataMap() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_FIRST_NAME, TEST_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_LAST_NAME);
        caseData.put(PETITIONER_EMAIL, TEST_USER_EMAIL);
        caseData.put(DIVORCE_OR_DISSOLUTION, DivorceOrDissolution.DIVORCE);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put(FINANCIAL_ORDER, NO);
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO);
        return caseData;
    }

    private void stubForDocAssembly() throws IOException {
        DOC_ASSEMBLY_SERVER.stubFor(
            post("/api/template-renditions")
                .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTH_TOKEN))
                .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(expectedDocAssemblyResponse("classpath:wiremock/responses/dg-assembly-response.json")))
        );
    }

    public void stubGetOrganisationEndpoint(String organisationId) throws JsonProcessingException {
        PRD_ORGANISATION_SERVER.stubFor(WireMock.get("/refdata/external/v1/organisations")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(
                    OrganisationsResponse
                        .builder()
                        .organisationIdentifier(organisationId)
                        .build()
                    )
                )
            )
        );
    }

    public void stubGetOrganisationEndpointForFailure() {
        PRD_ORGANISATION_SERVER.stubFor(WireMock.get("/refdata/external/v1/organisations")
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            )
        );
    }


    private static String expectedDocAssemblyResponse(String resourcePath) throws IOException {
        File jsonFile = ResourceUtils.getFile(resourcePath);
        return new String(Files.readAllBytes(jsonFile.toPath()));
    }

    static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                "doc_assembly.url=" + "http://localhost:" + DOC_ASSEMBLY_SERVER.port(),
                "prd.api.url=" + "http://localhost:" + PRD_ORGANISATION_SERVER.port()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
