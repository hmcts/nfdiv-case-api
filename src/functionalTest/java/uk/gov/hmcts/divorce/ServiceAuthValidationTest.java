package uk.gov.hmcts.divorce;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.model.CaseDetails;
import uk.gov.hmcts.divorce.model.CcdCallbackRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.TestResourceUtil.SUBMITTED_CALLBACK_URL;
import static uk.gov.hmcts.divorce.ccd.event.SaveAndClose.SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.constants.ControllerConstants.SERVICE_AUTHORIZATION;

@SpringBootTest
public class ServiceAuthValidationTest extends FunctionalTestSuite {
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

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
    }
}
