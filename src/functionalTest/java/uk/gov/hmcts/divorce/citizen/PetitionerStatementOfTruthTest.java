package uk.gov.hmcts.divorce.citizen;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.citizen.event.PetitionerStatementOfTruth.PETITIONER_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_START_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedCcdCallbackResponse;

public class PetitionerStatementOfTruthTest extends FunctionalTestSuite {


    private static final String ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-petitoner-statement-of-truth.json";
    private static final String ABOUT_TO_START_RESPONSE = "classpath:responses/ccd-callback-casedata-petitoner-statement-of-truth.json";

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValid() throws IOException {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .body(
                CallbackRequest
                    .builder()
                    .eventId(PETITIONER_STATEMENT_OF_TRUTH)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .data(caseData(ABOUT_TO_SUBMIT_REQUEST))
                            .build()
                    )
                    .build()
            )
            .when()
            .post(ABOUT_TO_START_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedCcdCallbackResponse(ABOUT_TO_START_RESPONSE),
            response.asString(),
            STRICT
        );
    }

}
