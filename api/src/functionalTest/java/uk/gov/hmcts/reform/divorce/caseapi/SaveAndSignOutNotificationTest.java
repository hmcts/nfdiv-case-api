package uk.gov.hmcts.reform.divorce.caseapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.divorce.caseapi.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution.DIVORCE;

@SpringBootTest
public class SaveAndSignOutNotificationTest extends FunctionalTestSuite {
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
}
