package uk.gov.hmcts.reform.divorce.caseapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.ccd.CcdCallbackRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution.DIVORCE;

@SpringBootTest
public class SaveAndSignOutNotificationTest {
    public static final String TEST_USER_EMAIL = "simulate-delivered@notifications.service.gov.uk";
    public static final String TEST_FIRST_NAME = "John";
    public static final String TEST_LAST_NAME = "Smith";

    public static final String D8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    public static final String D8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    public static final String D8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String DIVORCE_OR_DISSOLUTION = "divorceOrDissolution";
    public static final String NOTIFY_APPLICANT = "/notify-applicant";

    @Value("${test-url}")
    private String testUrl;

    @Test
    public void shouldSendEmailWhenAllTemplateParamsAreValid() {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(ccdCallbackRequestWithCaseData(
                Map.of(
                    D8_PETITIONER_FIRST_NAME, TEST_FIRST_NAME,
                    D8_PETITIONER_LAST_NAME, TEST_LAST_NAME,
                    D8_PETITIONER_EMAIL, TEST_USER_EMAIL,
                    DIVORCE_OR_DISSOLUTION, DIVORCE)
                )
            )
            .when()
            .post(NOTIFY_APPLICANT);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldFailWithBadRequestErrorWhenFirstAndLastNamesAreMissing() {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(ccdCallbackRequestWithCaseData(
                Map.of(
                    D8_PETITIONER_EMAIL, TEST_USER_EMAIL,
                    DIVORCE_OR_DISSOLUTION, DIVORCE)
                )
            )
            .when()
            .post(NOTIFY_APPLICANT);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.getBody().asString())
            .contains("{\"errors\":"
                + "[{\"error\":"
                + "\"BadRequestError\","
                + "\"message\":\"Missing personalisation: first name, last name\"}]");
    }

    @Test
    public void shouldFailValidationErrorWhenEmailIsMissing() {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(ccdCallbackRequestWithCaseData(
                Map.of(
                    DIVORCE_OR_DISSOLUTION, DIVORCE)
                )
            )
            .when()
            .post(NOTIFY_APPLICANT);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.getBody().asString())
            .contains("{\"errors\":"
                + "[{\"error\":\"ValidationError\","
                + "\"message\":\"email_address is a required property\"}]");
    }


    private CcdCallbackRequest ccdCallbackRequestWithCaseData(Map<String, Object> caseData) {
        return CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .data(caseData)
                    .build()
            )
            .build();
    }
}
