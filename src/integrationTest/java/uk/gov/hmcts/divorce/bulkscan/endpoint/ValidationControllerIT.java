package uk.gov.hmcts.divorce.bulkscan.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.populateD8OcrDataFields;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ValidationControllerIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Test
    public void givenValidOcrDataThenValidationSuccess() throws Exception {
        mockMvc.perform(post("/forms/D8/validate-ocr")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    OcrDataValidationRequest.builder()
                        .ocrDataFields(populateD8OcrDataFields())
                        .build()))
                .accept(APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponse("classpath:bulk-scan-d8-validation-success-response.json")));
    }

    @Test
    public void givenIncompleteOcrDataThenValidationWarnings() throws Exception {
        mockMvc.perform(post("/forms/D8/validate-ocr")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    OcrDataValidationRequest.builder()
                        .ocrDataFields(
                            List.of(
                                new OcrDataField("applicationForDivorce", "true"),
                                new OcrDataField("aSoleApplication", "true")
                            )
                        )
                        .build()))
                .accept(APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponse("classpath:bulk-scan-d8-validation-warning-response.json")));
    }

    @Test
    public void givenInvalidFormTypeThenValidationErrors() throws Exception {
        mockMvc.perform(post("/forms/invalid/validate-ocr")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    OcrDataValidationRequest.builder()
                        .ocrDataFields(
                            List.of(
                                new OcrDataField("applicationForDivorce", "true"),
                                new OcrDataField("aSoleApplication", "true")
                            )
                        )
                        .build()))
                .accept(APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponse("classpath:bulk-scan-d8-validation-error-response.json")));
    }

    @Test
    public void shouldAcceptEmptyList() throws Exception {
        mockMvc.perform(post("/forms/D8/validate-ocr")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    OcrDataValidationRequest.builder()
                        .ocrDataFields(
                            List.of()
                        )
                        .build()))
                .accept(APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(200));
    }

    @Test
    public void shouldAcceptEmptyBody() throws Exception {
        mockMvc.perform(post("/forms/D8/validate-ocr")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    OcrDataValidationRequest.builder().build()))
                .accept(APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(200));
    }

    @Test
    public void shouldAcceptEmptyRequest() throws Exception {
        mockMvc.perform(post("/forms/D8/validate-ocr")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .accept(APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(200));
    }
}
