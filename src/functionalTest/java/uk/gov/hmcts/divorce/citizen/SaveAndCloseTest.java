package uk.gov.hmcts.divorce.citizen;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.citizen.event.SaveAndClose.SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.SUBMITTED_CALLBACK_URL;

@SpringBootTest
public class SaveAndCloseTest extends FunctionalTestSuite {

    private static final String SUBMITTED_REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";

    @Test
    public void shouldSendEmailWhenAllTemplateParamsAreValid() throws IOException {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CallbackRequest
                    .builder()
                    .eventId(SAVE_AND_CLOSE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .data(caseData(SUBMITTED_REQUEST))
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SUBMITTED_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldFailWithBadRequestErrorWhenFirstAndLastNamesAreMissing() throws IOException {
        Map<String, Object> caseDataMapWithMissingParams = caseData(SUBMITTED_REQUEST);
        caseDataMapWithMissingParams.remove(APPLICANT_1_FIRST_NAME);
        caseDataMapWithMissingParams.remove(APPLICANT_1_LAST_NAME);

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CallbackRequest
                    .builder()
                    .eventId(SAVE_AND_CLOSE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .data(caseDataMapWithMissingParams)
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
    public void shouldFailValidationErrorWhenEmailIsMissing() throws IOException {
        Map<String, Object> caseDataMapWithMissingParams = caseData(SUBMITTED_REQUEST);
        caseDataMapWithMissingParams.remove(APPLICANT_1_EMAIL);

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CallbackRequest
                    .builder()
                    .eventId(SAVE_AND_CLOSE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .data(caseDataMapWithMissingParams)
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
