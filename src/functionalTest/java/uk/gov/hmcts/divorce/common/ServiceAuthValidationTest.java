package uk.gov.hmcts.divorce.common;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.common.model.CaseDetails;
import uk.gov.hmcts.divorce.common.model.CcdCallbackRequest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.citizen.event.SaveAndClose.SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.SUBMITTED_CALLBACK_URL;

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
