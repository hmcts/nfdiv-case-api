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
import uk.gov.hmcts.reform.divorce.caseapi.config.WebMvcConfig;
import uk.gov.hmcts.reform.divorce.caseapi.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.caseapi.service.solicitor.SolicitorCreatePetitionService;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SOLICITOR_CREATE_ABOUT_TO_START_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SOLICITOR_CREATE_ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.reform.divorce.caseapi.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.reform.divorce.caseapi.util.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SolicitorCreateController.class)
class SolicitorCreateControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SolicitorCreatePetitionService solicitorCreatePetitionService;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    public void shouldSetDefaultCaseDataValues() throws Exception {

        final CaseData updatedCaseDate = caseData();
        updatedCaseDate.setLanguagePreferenceWelsh(NO);

        final CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(objectMapper.convertValue(updatedCaseDate, new TypeReference<>() {
            }))
            .build();

        mockMvc
            .perform(
                post(SOLICITOR_CREATE_ABOUT_TO_START_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                    .content(objectMapper.writeValueAsBytes(callbackRequest())))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(ccdCallbackResponse)));
    }

    @Test
    public void shouldPopulateMissingRequirementsFieldsInCaseData() throws Exception {

        final CaseData updatedCaseData = caseData();

        when(solicitorCreatePetitionService.aboutToSubmit(
            any(CaseData.class),
            any(String.class),
            any(String.class)
        ))
            .thenReturn(updatedCaseData);

        final CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(objectMapper.convertValue(updatedCaseData, new TypeReference<>() {
            }))
            .build();

        mockMvc
            .perform(
                post(SOLICITOR_CREATE_ABOUT_TO_SUBMIT_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                    .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .content(objectMapper.writeValueAsBytes(callbackRequest())))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(ccdCallbackResponse)));
    }
}
