package uk.gov.hmcts.divorce.solicitor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.common.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStatementOfTruthPaySubmit.SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SolicitorSubmitApplicationTest extends FunctionalTestSuite {

    private static final String ABOUT_TO_SUBMIT_REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";
    private static final String ABOUT_TO_START_RESPONSE = "classpath:responses/ccd-callback-submit-application.json";
    private static final String VALID_ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-solicitor-submit-application-about-to-submit.json";
    private static final String VALID_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/ccd-callback-solicitor-submit-application-about-to-submit.json";
    private static final String INVALID_ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-solicitor-submit-application-about-to-submit-invalid.json";
    private static final String INVALID_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/ccd-callback-solicitor-submit-application-about-to-submit-invalid.json";

    @Test
    public void shouldUpdateCaseDataWithOrderSummaryAndAddSolCaseRolesWhenIssueFeeIsSuccessfullyRetrieved() throws Exception {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(createCaseInCcd().getId())
                    .data(caseData(ABOUT_TO_SUBMIT_REQUEST))
                    .build()
            )
            .build();

        Response response = triggerCallback(request, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedResponse(ABOUT_TO_START_RESPONSE),
            response.asString(),
            STRICT
        );
    }

    @Test
    public void shouldChangeStateToSubmittedIfPaymentProcessed() throws Exception {
        CallbackRequest request = CallbackRequest
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
            .build();

        final Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedResponse(VALID_ABOUT_TO_SUBMIT_RESPONSE),
            response.asString(),
            STRICT
        );
    }

    @Test
    public void shouldChangeStateToAwaitingPaymentConfirmationIfPaymentHasNotBeenProcessed() throws Exception {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1L)
                    .data(caseData(INVALID_ABOUT_TO_SUBMIT_REQUEST))
                    .state(SOTAgreementPayAndSubmitRequired.getName())
                    .build()
            )
            .build();

        final Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedResponse(INVALID_ABOUT_TO_SUBMIT_RESPONSE),
            response.asString(),
            STRICT
        );
    }
}
