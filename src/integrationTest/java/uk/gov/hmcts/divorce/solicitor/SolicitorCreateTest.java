package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.file.Files.readAllBytes;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_COSTS_CLAIM;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreate.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.BEARER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {SolicitorCreateTest.PropertiesInitializer.class})
class SolicitorCreateTest {

    private static final Instant INSTANT = Instant.parse("2021-04-06T16:00:00.00Z");
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private static final String SOLICITOR_CREATE_ABOUT_TO_SUBMIT = "classpath:solicitor-create-about-to-submit-response.json";
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

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    private static final WireMockServer DOC_ASSEMBLY_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeAll
    static void setUp() {
        DOC_ASSEMBLY_SERVER.start();
    }

    @AfterAll
    static void tearDown() {
        DOC_ASSEMBLY_SERVER.stop();
        DOC_ASSEMBLY_SERVER.resetAll();
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly()
        throws Exception {

        when(clock.instant()).thenReturn(INSTANT);
        when(clock.getZone()).thenReturn(ZONE_ID);

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

        System.out.println(jsonStringResponse);
        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_CREATE_ABOUT_TO_SUBMIT), STRICT);
    }

    private String expectedResponse(final String jsonFile) throws IOException {
        final File issueFeesResponseJsonFile = getFile(jsonFile);

        return new String(readAllBytes(issueFeesResponseJsonFile.toPath()));
    }

    private Map<String, Object> caseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_FIRST_NAME, TEST_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_LAST_NAME);
        caseData.put(PETITIONER_EMAIL, TEST_USER_EMAIL);
        caseData.put(DIVORCE_OR_DISSOLUTION, DivorceOrDissolution.DIVORCE);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put("languagePreferenceWelsh", NO);
        return caseData;
    }

    private void stubForDocAssembly() throws IOException {
        DOC_ASSEMBLY_SERVER.stubFor(
            post("/api/template-renditions")
                .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + TEST_AUTHORIZATION_TOKEN))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTH_TOKEN))
                .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(expectedDocAssemblyResponse("classpath:wiremock/responses/dg-assembly-response.json")))
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
                "doc_assembly.url=" + "http://localhost:" + DOC_ASSEMBLY_SERVER.port()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
