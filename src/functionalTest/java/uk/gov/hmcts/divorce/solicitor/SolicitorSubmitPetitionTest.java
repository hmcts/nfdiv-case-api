package uk.gov.hmcts.divorce.solicitor;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_COSTS_CLAIM;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.common.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStatementOfTruthPaySubmit.SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_START_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_SUBMIT_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedCcdCallbackResponse;

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
                CallbackRequest
                    .builder()
                    .eventId(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .id(createCaseInCcd().getId())
                            .data(caseData())
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

    @Test
    public void shouldChangeStateToAwaitingPaymentConfirmationIfValidCaseData()
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
                            .data(caseDataWithStatementOfTruth())
                            .state(SOTAgreementPayAndSubmitRequired.getName())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(ABOUT_TO_SUBMIT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedCcdCallbackResponse("classpath:responses/ccd-callback-solicitor-submit-petition-about-to-submit.json"),
            response.asString(),
            STRICT
        );
    }

    private Map<String, Object> caseDataWithStatementOfTruth() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_FIRST_NAME, TEST_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_LAST_NAME);
        caseData.put(PETITIONER_EMAIL, TEST_USER_EMAIL);
        caseData.put(DIVORCE_OR_DISSOLUTION, DIVORCE);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put("statementOfTruth", YES);
        caseData.put("solSignStatementOfTruth", YES);
        return caseData;
    }
}
