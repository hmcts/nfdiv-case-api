package uk.gov.hmcts.divorce.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.model.CaseDetails;
import uk.gov.hmcts.divorce.api.model.CcdCallbackRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.api.TestResourceUtil.SUBMITTED_CALLBACK_URL;
import static uk.gov.hmcts.divorce.api.ccd.event.SaveAndClose.SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.api.ccd.model.enums.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.api.constants.ControllerConstants.SERVICE_AUTHORIZATION;

@SpringBootTest
public class SaveAndCloseTest extends FunctionalTestSuite {
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
                    .eventId(SAVE_AND_CLOSE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseData())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SUBMITTED_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldFailWithBadRequestErrorWhenFirstAndLastNamesAreMissing() {
        CaseData caseDataWithMissingParams = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .petitionerEmail(TEST_USER_EMAIL)
            .build();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CcdCallbackRequest
                    .builder()
                    .eventId(SAVE_AND_CLOSE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseDataWithMissingParams)
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SUBMITTED_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.getBody().asString())
            .contains("{\"errors\":"
                + "[{\"error\":"
                + "\"BadRequestError\","
                + "\"message\":\"Missing personalisation: first name, last name\"}]");
    }

    @Test
    public void shouldFailValidationErrorWhenEmailIsMissing() {
        CaseData caseDataWithMissingParams = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CcdCallbackRequest
                    .builder()
                    .eventId(SAVE_AND_CLOSE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseDataWithMissingParams)
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SUBMITTED_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.getBody().asString())
            .contains("{\"errors\":"
                + "[{\"error\":\"ValidationError\","
                + "\"message\":\"email_address is a required property\"}]");
    }
}
