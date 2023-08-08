package uk.gov.hmcts.divorce.bulkscan.endpoint;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.TransformationInput;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType.D8S;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.inputScannedDocuments;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BulkScanCaseTransformationControllerIT {

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
    public void shouldSuccessfullyTransformD8FormWithoutWarnings() throws Exception {
        String validApplicationOcrJson = loadJson("src/integrationTest/resources/valid-d8-ocr.json");
        List<OcrDataField> ocrDataFields = OBJECT_MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        String response = mockMvc.perform(post("/transform-exception-record")
                .contentType(APPLICATION_JSON_VALUE)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(
                    OBJECT_MAPPER.writeValueAsString(
                        TransformationInput
                            .builder()
                            .formType(D8.getName())
                            .ocrDataFields(ocrDataFields)
                            .scannedDocuments(inputScannedDocuments(D8))
                            .build()
                    )
                )
                .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

        // dateSubmitted and document ids are compared using ${json-unit.any-string}
        // assertion will fail if the above value is missing
        assertThatJson(response)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(
                json(
                    expectedResponse("classpath:d8-transformation-success-response.json")
                )
            );
    }

    @Test
    public void shouldSuccessfullyTransformD8SFormWithoutWarnings() throws Exception {
        String validApplicationOcrJson = loadJson("src/integrationTest/resources/valid-d8s-ocr.json");
        List<OcrDataField> ocrDataFields = OBJECT_MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        String response = mockMvc.perform(post("/transform-exception-record")
                .contentType(APPLICATION_JSON_VALUE)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(
                    OBJECT_MAPPER.writeValueAsString(
                        TransformationInput
                            .builder()
                            .formType(D8S.getName())
                            .ocrDataFields(ocrDataFields)
                            .scannedDocuments(inputScannedDocuments(D8S))
                            .build()
                    )
                )
                .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

        // dateSubmitted and document ids are compared using ${json-unit.any-string}
        // assertion will fail if the above value is missing
        assertThatJson(response)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(
                json(
                    expectedResponse("classpath:d8s-transformation-success-response.json")
                )
            );
    }

    @Test
    public void shouldSuccessfullyTransformD8FormWithWarnings() throws Exception {
        String validApplicationOcrJson = loadJson("src/integrationTest/resources/valid-ocr-with-warnings.json");
        List<OcrDataField> ocrDataFields = OBJECT_MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        String response = mockMvc.perform(post("/transform-exception-record")
                .contentType(APPLICATION_JSON_VALUE)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(
                    OBJECT_MAPPER.writeValueAsString(
                        TransformationInput
                            .builder()
                            .formType(D8.getName())
                            .ocrDataFields(ocrDataFields)
                            .scannedDocuments(inputScannedDocuments(D8))
                            .build()
                    )
                )
                .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

        assertThatJson(response)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(
                json(
                    expectedResponse("classpath:transformation-success-warning-response.json")
                )
            );
    }

    @Test
    public void shouldThrowUnsupportedFormTypeExceptionWhenFormIsNotRecognised() throws Exception {
        String validApplicationOcrJson = loadJson("src/integrationTest/resources/valid-d8-ocr.json");
        List<OcrDataField> ocrDataFields = OBJECT_MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        String response = mockMvc.perform(post("/transform-exception-record")
                .contentType(APPLICATION_JSON_VALUE)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(
                    OBJECT_MAPPER.writeValueAsString(
                        TransformationInput
                            .builder()
                            .formType("invalidFormType")
                            .ocrDataFields(ocrDataFields)
                            .build()
                    )
                )
                .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

        assertThatJson(response)
            .isEqualTo(
                json(
                    expectedResponse("classpath:invalid-form-response-transformation.json")
                )
            );
    }
}
