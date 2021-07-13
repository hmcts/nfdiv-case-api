package uk.gov.hmcts.divorce.common;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSaveAndClose.CITIZEN_SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class ServiceAuthValidationTest extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";

    @Test
    public void shouldReturn401WhenServiceAuthTokenIsInvalid() throws IOException {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, "Bearer invalid_token")
            .body(
                CallbackRequest
                    .builder()
                    .eventId(CITIZEN_SAVE_AND_CLOSE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .data(caseData(REQUEST))
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldReturn403WhenServiceIsNotAuthorised() throws IOException {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate("ccd_gw"))
            .body(
                CallbackRequest
                    .builder()
                    .eventId(CITIZEN_SAVE_AND_CLOSE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .data(caseData(REQUEST))
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
    }
}
