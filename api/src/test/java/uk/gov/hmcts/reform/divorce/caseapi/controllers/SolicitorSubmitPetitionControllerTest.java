package uk.gov.hmcts.reform.divorce.caseapi.controllers;


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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.caseapi.TestAuthConfiguration;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.caseapi.service.SolicitorSubmitPetitionService;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.FeeItem;
import uk.gov.hmcts.reform.divorce.ccd.model.FeeValue;
import uk.gov.hmcts.reform.divorce.ccd.model.OrderSummary;

import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SUBMIT_PETITION_API_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.caseData;
import static uk.gov.hmcts.reform.divorce.ccd.model.FeeValue.getValueInPence;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SolicitorSubmitPetitionController.class)
@Import(TestAuthConfiguration.class)
public class SolicitorSubmitPetitionControllerTest {

    @MockBean
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenOrderSummaryIsSet() throws Exception {
        OrderSummary orderSummary = getOrderSummary();

        CaseData updatedCaseDate = caseData();
        updatedCaseDate.setOrderSummary(orderSummary);

        when(solicitorSubmitPetitionService.getOrderSummary())
            .thenReturn(orderSummary);

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(objectMapper.convertValue(updatedCaseDate, new TypeReference<Map<String, Object>>() {
            }))
            .build();

        mockMvc.perform(
            post(SUBMIT_PETITION_API_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsBytes(callbackRequest()))
        ).andExpect(
            status().isOk()
        ).andExpect(
            content().string(objectMapper.writeValueAsString(ccdCallbackResponse))
        );

        verify(solicitorSubmitPetitionService).getOrderSummary();
        verifyNoMoreInteractions(solicitorSubmitPetitionService);
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

    private OrderSummary getOrderSummary() {
        return OrderSummary
            .builder()
            .fees(singletonList(getFeeItem()))
            .build();
    }

    private FeeItem getFeeItem() {
        return FeeItem
            .builder()
            .value(
                FeeValue
                    .builder()
                    .feeAmount(getValueInPence(10.50))
                    .feeCode("FEECODE1")
                    .feeDescription("Issue Petition Fee")
                    .feeVersion(String.valueOf(1))
                    .build()
            )
            .build();
    }
}
