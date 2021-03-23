package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.divorce.caseapi.clients.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.caseapi.config.WebMvcConfig;
import uk.gov.hmcts.reform.divorce.caseapi.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.reform.divorce.caseapi.service.SolicitorSubmitPetitionService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SUBMIT_PETITION_API_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.getFeeResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@AutoConfigureMockMvc
public class SolicitorSubmitPetitionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeEach
    public void setUp() {
        WireMock.reset();
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenOrderSummaryIsSet() throws Exception {
        stubFor(
            get(
                urlEqualTo(
                    "/fees-register/fees/lookup"
                        + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family%20court&service=divorce"
                )
            ).willReturn(
                aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(
                        objectMapper.writeValueAsString(getFeeResponse())
                    )
            )
        );

        mockMvc.perform(post(SUBMIT_PETITION_API_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest()))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdCallbackResponse())
            );
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenOrderSummaryIsSet1() throws Exception {
        stubFor(
            get(
                urlEqualTo(
                    "/fees-register/fees/lookup"
                        + "?channel=default&event=issue&jurisdiction1=family&jurisdiction2=family%20court&service=divorce"
                )
            ).willReturn(
                aResponse()
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withStatusMessage("Fee event not found")
            )
        );

        mockMvc.perform(post(SUBMIT_PETITION_API_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest()))
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

    private String expectedCcdCallbackResponse() throws IOException {
        File issueFeesResponseJsonFile = ResourceUtils.getFile("classpath:wiremock/responses/issue-fees-response.json");

        return new String(Files.readAllBytes(issueFeesResponseJsonFile.toPath()));
    }
}

