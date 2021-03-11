package uk.gov.hmcts.reform.divorce.caseapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.divorce.caseapi.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SAVE_AND_CLOSE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SUBMITTED_WEBHOOK;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution.DIVORCE;

@SpringBootTest
public class SaveAndSignOutNotificationTest {
    public static final String TEST_USER_EMAIL = "simulate-delivered@notifications.service.gov.uk";
    public static final String TEST_FIRST_NAME = "John";
    public static final String TEST_LAST_NAME = "Smith";

    public static final String SAVE_AND_SIGN_OUT_CALLBACK_URL = StringUtils.join(
        "/", SAVE_AND_CLOSE, SUBMITTED_WEBHOOK
    );

    @Value("${test-url}")
    private String testUrl;

    @Value("${idam.s2s-auth.url}")
    private String s2sUrl;

    @Value("${s2s.name}")
    private String s2sName;

    @Test
    public void shouldSendEmailWhenAllTemplateParamsAreValid() {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CcdCallbackRequest
                    .builder()
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseData())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SAVE_AND_SIGN_OUT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldFailWithBadRequestErrorWhenFirstAndLastNamesAreMissing() {
        CaseData caseDataWithMissingParams = new CaseData();
        caseDataWithMissingParams.setDivorceOrDissolution(DIVORCE);
        caseDataWithMissingParams.setD8PetitionerEmail(TEST_USER_EMAIL);

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CcdCallbackRequest
                    .builder()
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseDataWithMissingParams)
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SAVE_AND_SIGN_OUT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.getBody().asString())
            .contains("{\"errors\":"
                + "[{\"error\":"
                + "\"BadRequestError\","
                + "\"message\":\"Missing personalisation: first name, last name\"}]");
    }

    @Test
    public void shouldFailValidationErrorWhenEmailIsMissing() {
        CaseData caseDataWithMissingParams = new CaseData();
        caseDataWithMissingParams.setDivorceOrDissolution(DIVORCE);

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CcdCallbackRequest
                    .builder()
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseDataWithMissingParams)
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SAVE_AND_SIGN_OUT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.getBody().asString())
            .contains("{\"errors\":"
                + "[{\"error\":\"ValidationError\","
                + "\"message\":\"email_address is a required property\"}]");
    }

    @Test
    public void shouldReturn401WhenServiceAuthTokenIsInvalid() {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, "Bearer invalid_token")
            .body(
                CcdCallbackRequest
                    .builder()
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseData())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SAVE_AND_SIGN_OUT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldReturn403WhenServiceIsNotAuthorised() {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor("ccd_gw"))
            .body(
                CcdCallbackRequest
                    .builder()
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseData())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SAVE_AND_SIGN_OUT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
    }

    private CaseData caseData() {
        CaseData caseData = new CaseData();
        caseData.setD8PetitionerFirstName(TEST_FIRST_NAME);
        caseData.setD8PetitionerLastName(TEST_LAST_NAME);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setD8PetitionerEmail(TEST_USER_EMAIL);
        return caseData;
    }

    private String generateServiceAuthTokenFor(String s2sName) {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.s2sUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(Map.of("microservice", s2sName))
            .when()
            .post("/testing-support/lease")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        return "Bearer " + response.getBody().print();
    }
}
