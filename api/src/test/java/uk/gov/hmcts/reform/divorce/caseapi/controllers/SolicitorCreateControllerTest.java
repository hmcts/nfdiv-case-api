package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.divorce.caseapi.config.WebMvcConfig;
import uk.gov.hmcts.reform.divorce.caseapi.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SOLICITOR_CREATE_API_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.caseData;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.getDefaultOrderSummary;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SolicitorCreateController.class)
class SolicitorCreateControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    public void shouldSetDefaultCaseDataValues() throws Exception {

        final OrderSummary orderSummary = getDefaultOrderSummary();

        final CaseData updatedCaseDate = caseData();
        updatedCaseDate.setLanguagePreferenceWelsh(YesOrNo.NO);

        final CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(objectMapper.convertValue(updatedCaseDate, new TypeReference<>() {
            }))
            .build();

        mockMvc
            .perform(
                post(SOLICITOR_CREATE_API_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                    .content(objectMapper.writeValueAsBytes(callbackRequest())))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(ccdCallbackResponse)));
    }
}
