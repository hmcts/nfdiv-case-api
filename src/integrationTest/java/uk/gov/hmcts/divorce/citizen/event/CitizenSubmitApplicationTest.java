package uk.gov.hmcts.divorce.citizen.event;

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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.testutil.FeesWireMock;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.Objects.requireNonNull;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitApplication.CITIZEN_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesLookup;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesNotFound;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseDataMap;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    FeesWireMock.PropertiesInitializer.class,
})
public class CitizenSubmitApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        FeesWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        FeesWireMock.stopAndReset();
    }

    @Test
    public void givenValidCaseDataThenReturnResponseWithNoErrors() throws Exception {
        stubForFeesLookup(TestDataHelper.getFeeResponseAsJson());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(validApplicant1CaseDataMap(), CITIZEN_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // marriageDate and payments.id are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(actualResponse)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse()));
    }

    @Test
    public void givenValidCaseDataWithHwfThenReturnResponseWithNoErrors() throws Exception {
        stubForFeesLookup(TestDataHelper.getFeeResponseAsJson());

        CaseData caseData = validApplicant1CaseDataMap();
        caseData.setHelpWithFeesAppliedForFees(YesOrNo.YES);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, CITIZEN_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // marriageDate and payments.id are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(actualResponse)
            .isEqualTo(json(expectedCcdAboutToStartCallbackWithHwfSuccessfulResponse()));
    }

    @Test
    public void givenFeeEventIsNotAvailableWhenAboutToStartCallbackIsInvokedThenReturn404FeeEventNotFound()
        throws Exception {
        stubForFeesNotFound();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithOrderSummary(), CITIZEN_SUBMIT)))
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
    public void givenInvalidCaseDataThenReturnResponseWithErrors() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(), CITIZEN_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedCcdAboutToStartCallbackErrorResponse()));
    }

    @Test
    public void givenRequestBodyIsNullWhenEndpointInvokedThenReturnBadRequest() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    private String expectedCcdAboutToStartCallbackErrorResponse() throws IOException {
        File invalidCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-start-statement-of-truth-errors.json");

        return new String(Files.readAllBytes(invalidCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToStartCallbackSuccessfulResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-start-statement-of-truth.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToStartCallbackWithHwfSuccessfulResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-start-help-with-fees.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }
}
