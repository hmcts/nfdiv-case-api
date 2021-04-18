package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.divorce.TestConstants;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitPetitionService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStatementOfTruthPaySubmit.SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT;
import static uk.gov.hmcts.divorce.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.util.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.util.TestDataHelper.getFeeResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {SolicitorStatementOfTruthPaySubmitTest.PropertiesInitializer.class})
public class SolicitorStatementOfTruthPaySubmitTest {

    private static final String CASE_WORKER_TOKEN = "test-caseworker-token";

    private static final String SOLICITOR_ROLE = "caseworker-divorce-solicitor";

    private static final String CASEWORKER_ROLE = "caseworker-divorce";

    private static final String SERVICE_AUTH_TOKEN = "test-service-auth-token";

    private static final String BEARER = "Bearer ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CcdAccessService ccdAccessService;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    private static final WireMockServer IDAM_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private static final WireMockServer CASE_DATA_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private static final WireMockServer FEES_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeAll
    static void setUp() {
        IDAM_SERVER.start();
        CASE_DATA_SERVER.start();
        FEES_SERVER.start();
    }

    @AfterAll
    static void tearDown() {
        IDAM_SERVER.stop();
        IDAM_SERVER.resetAll();

        CASE_DATA_SERVER.stop();
        CASE_DATA_SERVER.resetAll();

        FEES_SERVER.stop();
        FEES_SERVER.resetAll();
    }


    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenOrderSummaryAndSolicitorRolesAreSet()
        throws Exception {
        stubForFeesLookup();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        stubForIdamDetails(CASE_WORKER_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        stubForIdamToken();

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        stubForCcdCaseRoles();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(), SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdCallbackResponse())
            );

        verify(serviceTokenGenerator).generate();
        verifyNoMoreInteractions(serviceTokenGenerator);
    }

    @Test
    public void givenFeeEventIsNotAvailableWhenCallbackIsInvokedThenReturn404FeeEventNotFound()
        throws Exception {
        stubForFeesNotFound();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(), SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isNotFound()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.NotFound.class)
            )
            .andExpect(
                result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                    .contains("404 Fee event not found")
            );
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedAndIdamUserRetrievalThrowsUnauthorizedThen401IsReturned()
        throws Exception {
        stubForFeesLookup();

        stubForIdamFailure();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(), SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Unauthorized.class)
            )
            .andExpect(
                result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                    .contains("Invalid idam credentials")
            );
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedAndCcdCaseRolesUpdateThrowsForbiddenExceptionThen403IsReturned()
        throws Exception {
        stubForFeesLookup();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        stubForIdamDetails(CASE_WORKER_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        stubForIdamToken();

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        stubForCcdCaseRolesUpdateFailure();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(), SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isForbidden()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Forbidden.class)
            );
    }

    private void stubForCcdCaseRoles() {
        CASE_DATA_SERVER.stubFor(put(urlMatching("/cases/[0-9]+/users/[0-9]+"))
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER + CASE_WORKER_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTH_TOKEN))
            .withRequestBody(new EqualToJsonPattern(
                "{\"user_id\" : \"1\", \"case_roles\":[\"[CREATOR]\",\"[PETSOLICITOR]\"]}",
                true,
                true)
            )
            .willReturn(aResponse().withStatus(200))
        );
    }

    private void stubForCcdCaseRolesUpdateFailure() {
        CASE_DATA_SERVER.stubFor(put(urlMatching("/cases/[0-9]+/users/[0-9]+"))
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER + CASE_WORKER_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(SERVICE_AUTH_TOKEN))
            .withRequestBody(new EqualToJsonPattern(
                "{\"user_id\" : \"1\", \"case_roles\":[\"[CREATOR]\",\"[PETSOLICITOR]\"]}",
                true,
                true)
            )
            .willReturn(aResponse().withStatus(403))
        );
    }

    private void stubForIdamToken() throws UnsupportedEncodingException {
        IDAM_SERVER.stubFor(post("/o/token")
            .withRequestBody(new EqualToPattern(tokenBody()))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{ \"access_token\" : \"" + CASE_WORKER_TOKEN + "\" }")))
        ;
    }

    private void stubForIdamDetails(String testAuthorizationToken, String solicitorUserId, String solicitorRole) {
        IDAM_SERVER.stubFor(get("/details")
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER + testAuthorizationToken))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(getUserDetailsForRole(solicitorUserId, solicitorRole)))
        );
    }

    private void stubForIdamFailure() {
        IDAM_SERVER.stubFor(get("/details")
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER + TEST_AUTHORIZATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.UNAUTHORIZED.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("Invalid idam credentials"))
        );
    }

    private void stubForFeesLookup() throws JsonProcessingException {
        FEES_SERVER.stubFor(get("/fees-register/fees/lookup"
                + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family+court&service=divorce"
            ).willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(getFeeResponse())))
        );
    }

    private void stubForFeesNotFound() {
        FEES_SERVER.stubFor(get(urlEqualTo(
            "/fees-register/fees/lookup"
                + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family+court&service=divorce"
            )
            ).willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withStatusMessage("Fee event not found")
            )
        );
    }

    private String expectedCcdCallbackResponse() throws IOException {
        File issueFeesResponseJsonFile = ResourceUtils.getFile("classpath:wiremock/responses/issue-fees-response.json");

        return new String(Files.readAllBytes(issueFeesResponseJsonFile.toPath()));
    }

    private String getUserDetailsForRole(String userId, String role) {
        return "{\"id\":\"" + userId
            + "\",\"email\":\"" + TEST_USER_EMAIL
            + "\",\"forename\":\"forename\",\"surname\":\"Surname\",\"roles\":[\"" + role + "\"]}";
    }

    private String tokenBody() throws UnsupportedEncodingException {
        return "password=dummy"
            + "&grant_type=password"
            + "&scope=" + encode("openid profile roles", UTF_8.name())
            + "&client_secret=BBBBBBBBBBBBBBBB"
            + "&redirect_uri=" + encode("http://localhost:3001/oauth2/callback", UTF_8.name())
            + "&client_id=divorce"
            + "&username=" + encode("dummycaseworker@test.com", UTF_8.name());
    }

    static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                "idam.api.url=" + "http://localhost:" + IDAM_SERVER.port(),
                "core_case_data.api.url=" + "http://localhost:" + CASE_DATA_SERVER.port(),
                "fee.api.baseUrl=" + "http://localhost:" + FEES_SERVER.port()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}

