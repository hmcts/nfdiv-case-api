package uk.gov.hmcts.divorce.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.testutil.ServiceAuthenticationGenerator;

import java.io.IOException;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.populateD8OcrDataFields;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.populateKeyValue;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class ValidationControllerFT {

    @Value("${test-url}")
    protected String testUrl;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final String RESPONSE_SUCCESS = "classpath:responses/response-bulk-scan-d8-validation-success.json";
    private static final String RESPONSE_WARNINGS = "classpath:responses/response-bulk-scan-d8-validation-warning.json";
    private static final String RESPONSE_ERRORS = "classpath:responses/response-bulk-scan-d8-validation-error.json";

    @Test
    public void shouldPassValidationGivenValidOcrDataProvided() throws IOException {
        OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(populateD8OcrDataFields())
            .build();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .body(request)
            .when()
            .post("/forms/D8/validate-ocr");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_SUCCESS)));
    }

    @Test
    public void shouldReturnWarningsGivenIncompleteOcrDataProvided() throws IOException {
        OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("applicationForDivorce", "true"),
                    populateKeyValue("aSoleApplication", "true")
                )
            )
            .build();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .body(request)
            .when()
            .post("/forms/D8/validate-ocr");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_WARNINGS)));
    }

    @Test
    public void shouldReturnErrorWhenInvalidFormTypeProvided() throws IOException {
        OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(populateD8OcrDataFields())
            .build();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .body(request)
            .when()
            .post("/forms/invalid/validate-ocr");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_ERRORS)));
    }
}
