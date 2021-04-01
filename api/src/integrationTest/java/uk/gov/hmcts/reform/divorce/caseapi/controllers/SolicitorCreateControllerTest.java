package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.divorce.caseapi.config.WebMvcConfig;
import uk.gov.hmcts.reform.divorce.caseapi.config.interceptors.RequestInterceptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SOLICITOR_CREATE_ABOUT_TO_START_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.util.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SolicitorCreateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitorCreateController solicitorCreateController;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;


    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedLanguagePreferenceIsSet()
        throws Exception {

        String jsonStringResponse = mockMvc.perform(post(SOLICITOR_CREATE_ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest()))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedCcdCallbackResponse(), STRICT);
    }

    private String expectedCcdCallbackResponse() throws IOException {
        File issueFeesResponseJsonFile = ResourceUtils.getFile("classpath:set-language-preference-response.json");

        return new String(Files.readAllBytes(issueFeesResponseJsonFile.toPath()));
    }
}
