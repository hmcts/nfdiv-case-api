package uk.gov.hmcts.divorce.solicitor;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStatementOfTruthPaySubmit.SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_START_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_SUBMIT_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedCcdCallbackResponse;

@SpringBootTest
public class SolicitorSubmitApplicationTest extends FunctionalTestSuite {

    private static final String ABOUT_TO_SUBMIT_REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";
    private static final String ABOUT_TO_START_RESPONSE = "classpath:responses/ccd-callback-submit-application.json";
    private static final String VALID_ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-solicitor-submit-application-about-to-submit.json";
    private static final String VALID_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/ccd-callback-solicitor-submit-application-about-to-submit.json";

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
                CallbackRequest
                    .builder()
                    .eventId(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .id(createCaseInCcd().getId())
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

    @Test
    public void shouldChangeStateToAwaitingPaymentConfirmationIfValidCaseData()
        throws Exception {

        final Map<String, Object> caseData = caseData(VALID_ABOUT_TO_SUBMIT_REQUEST);

        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .header(HttpHeaders.AUTHORIZATION, generateIdamTokenForSolicitor())
            .body(
                CallbackRequest
                    .builder()
                    .eventId(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .id(1L)
                            .data(caseData(VALID_ABOUT_TO_SUBMIT_REQUEST))
                            .state(SOTAgreementPayAndSubmitRequired.getName())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(ABOUT_TO_SUBMIT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedCcdCallbackResponse(VALID_ABOUT_TO_SUBMIT_RESPONSE),
            response.asString(),
            STRICT
        );
    }
}
