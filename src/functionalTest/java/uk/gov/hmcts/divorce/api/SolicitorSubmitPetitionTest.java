package uk.gov.hmcts.divorce.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.divorce.api.model.CaseDetails;
import uk.gov.hmcts.divorce.api.model.CcdCallbackRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.api.TestResourceUtil.ABOUT_TO_START_CALLBACK_URL;
import static uk.gov.hmcts.divorce.api.TestResourceUtil.expectedCcdCallbackResponse;
import static uk.gov.hmcts.divorce.api.ccd.event.solicitor.SolicitorStatementOfTruthPaySubmit.SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT;
import static uk.gov.hmcts.divorce.api.constants.ControllerConstants.SERVICE_AUTHORIZATION;

@SpringBootTest
public class SolicitorSubmitPetitionTest extends FunctionalTestSuite {

    @Test
    public void shouldUpdateCaseDataWithOrderSummaryAndAddSolCaseRolesWhenIssueFeeIsSuccessfullyRetrieved()
        throws Exception {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .header(HttpHeaders.AUTHORIZATION, generateIdamTokenForSolicitor())
            .body(
                CcdCallbackRequest
                    .builder()
                    .eventId(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseId(createCaseInCcd().getId())
                            .caseData(caseData())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(ABOUT_TO_START_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedCcdCallbackResponse("classpath:responses/ccd-callback-submit-petition.json"),
            response.asString(),
            STRICT
        );
    }
}
