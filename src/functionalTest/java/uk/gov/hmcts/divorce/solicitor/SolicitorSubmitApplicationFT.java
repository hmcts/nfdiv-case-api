package uk.gov.hmcts.divorce.solicitor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitApplication.SOLICITOR_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SolicitorSubmitApplicationFT extends FunctionalTestSuite {

    private static final String ABOUT_TO_SUBMIT_REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";
    private static final String ABOUT_TO_START_RESPONSE = "classpath:responses/response-solicitor-submit-application.json";
    private static final String VALID_ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-solicitor-submit-application-about-to-submit.json";
    private static final String VALID_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/response-solicitor-submit-application-about-to-submit.json";

    @Test
    @Disabled
    public void shouldUpdateCaseDataWithOrderSummaryAndAddSolCaseRolesWhenIssueFeeIsSuccessfullyRetrieved() throws Exception {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(SOLICITOR_SUBMIT)
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

    /**
     * If this test fails please rerun the test as it will be most likely due to payments being made by
     * other PR's/master build with same PBA number
     * You will see an error message like below in your jenkins console
     * Different value found in node "errors",
     * expected: null
     * but was: <["Payment request failed. Please try again after 2 minutes with a different Payment Account,
     * or alternatively use a different payment method.
     * For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com."]>.
     **/
    @Test
    public void shouldChangeStateToSubmittedIfPaymentProcessed() throws Exception {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(SOLICITOR_SUBMIT)
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1L)
                    .caseTypeId(CASE_TYPE)
                    .data(caseData(VALID_ABOUT_TO_SUBMIT_REQUEST))
                    .state(Draft.getName())
                    .build()
            )
            .build();

        final Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(VALID_ABOUT_TO_SUBMIT_RESPONSE)));
    }
}
