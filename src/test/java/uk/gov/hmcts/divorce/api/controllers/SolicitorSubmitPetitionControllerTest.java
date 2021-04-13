package uk.gov.hmcts.divorce.api.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.api.config.WebMvcConfig;
import uk.gov.hmcts.divorce.api.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.api.model.CcdCallbackResponse;
import uk.gov.hmcts.divorce.api.service.CcdAccessService;
import uk.gov.hmcts.divorce.api.service.SolicitorSubmitPetitionService;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;

import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.api.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.api.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.api.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.api.TestConstants.SUBMIT_PETITION_API_URL;
import static uk.gov.hmcts.divorce.api.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.api.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.api.util.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.api.util.TestDataHelper.getDefaultOrderSummary;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SolicitorSubmitPetitionController.class)
public class SolicitorSubmitPetitionControllerTest {

    @MockBean
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private CcdAccessService ccdAccessService;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenOrderSummaryAndSolicitorRolesAreSet()
        throws Exception {
        OrderSummary orderSummary = getDefaultOrderSummary();

        CaseData updatedCaseData = caseData();
        updatedCaseData.setSolApplicationFeeOrderSummary(orderSummary);

        when(solicitorSubmitPetitionService.getOrderSummary())
            .thenReturn(orderSummary);

        doNothing()
            .when(ccdAccessService)
            .addPetitionerSolicitorRole(
                anyString(),
                anyLong()
            );

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(objectMapper.convertValue(updatedCaseData, new TypeReference<Map<String, Object>>() {
            }))
            .build();

        mockMvc.perform(
            post(SUBMIT_PETITION_API_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsBytes(callbackRequest()))
        ).andExpect(
            status().isOk()
        ).andExpect(
            content().string(objectMapper.writeValueAsString(ccdCallbackResponse))
        );

        verify(solicitorSubmitPetitionService).getOrderSummary();
        verify(ccdAccessService).addPetitionerSolicitorRole(
            anyString(),
            anyLong()
        );
        verifyNoMoreInteractions(solicitorSubmitPetitionService, ccdAccessService);
    }

    @Test
    public void shouldReturn404WhenCallbackIsInvokedAndServiceThrowsException() throws Exception {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "feeLookupNotFound",
            Response.builder()
                .request(request)
                .status(404)
                .headers(Collections.emptyMap())
                .reason("Fee Not found")
                .build()
        );

        doThrow(feignException).when(solicitorSubmitPetitionService).getOrderSummary();

        mockMvc.perform(
            post(SUBMIT_PETITION_API_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsBytes(callbackRequest()))
        ).andExpect(
            status().isNotFound()
        ).andExpect(
            result -> assertThat(result.getResolvedException())
                .isExactlyInstanceOf(FeignException.NotFound.class)
        ).andExpect(
            result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                .contains("404 Fee Not found")
        );
    }

    @Test
    public void shouldReturn401WhenCallbackIsInvokedAndAddingRolesThrowsException()
        throws Exception {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "idamRequestFailed",
            Response.builder()
                .request(request)
                .status(401)
                .headers(Collections.emptyMap())
                .reason("Failed to retrieve Idam user")
                .build()
        );

        doThrow(feignException).when(ccdAccessService).addPetitionerSolicitorRole(
            anyString(),
            anyLong()
        );

        mockMvc.perform(
            post(SUBMIT_PETITION_API_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsBytes(callbackRequest()))
        ).andExpect(
            status().isUnauthorized()
        ).andExpect(
            result -> assertThat(result.getResolvedException())
                .isExactlyInstanceOf(FeignException.Unauthorized.class)
        ).andExpect(
            result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                .contains("Failed to retrieve Idam user")
        );
    }
}
