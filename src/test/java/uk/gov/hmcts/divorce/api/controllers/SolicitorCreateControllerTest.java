package uk.gov.hmcts.divorce.api.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.config.WebMvcConfig;
import uk.gov.hmcts.divorce.api.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.api.model.CcdCallbackResponse;
import uk.gov.hmcts.divorce.api.service.solicitor.SolicitorCreatePetitionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.api.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.api.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.api.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.api.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.api.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.api.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.api.ccd.event.solicitor.SolicitorCreate.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.api.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.api.util.TestDataHelper.caseData;

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
                post(ABOUT_TO_START_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                    .content(objectMapper.writeValueAsBytes(callbackRequest(caseData(), SOLICITOR_CREATE))))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(ccdCallbackResponse)));
    }

    public void shouldPopulateMissingRequirementsFieldsInCaseData() throws Exception {

        final CaseData updatedCaseData = caseData();

        when(solicitorCreatePetitionService.aboutToSubmit(
            any(CaseData.class),
            any(Long.class),
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
                post(ABOUT_TO_SUBMIT_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                    .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .content(objectMapper.writeValueAsBytes(callbackRequest(caseData(), SOLICITOR_CREATE))))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(ccdCallbackResponse)));
    }
}
