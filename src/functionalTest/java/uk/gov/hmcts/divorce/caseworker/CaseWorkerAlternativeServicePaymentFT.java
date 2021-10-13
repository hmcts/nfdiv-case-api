package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.CASEWORKER_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
@Disabled
public class CaseWorkerAlternativeServicePaymentFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-sole-awaiting-service-payment.json";

    @Test
    public void shouldReturnDeemedAndDispensedFeeWhenAlternativeServiceTypeIsDeemed() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST);

        caseData.put("alternativeServiceType", "deemed");

        Response response = triggerCallback(caseData, CASEWORKER_SERVICE_PAYMENT,
            "/callbacks/about-to-start?page=alternativeServicePayment");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-confirm-payment-deemed.json"
            )));

    }

    @Test
    public void shouldReturnBailiffFeeWhenAlternativeServiceTypeIsBailiff() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST);

        caseData.put("alternativeServiceType", "bailiff");

        Response response = triggerCallback(caseData, CASEWORKER_SERVICE_PAYMENT,
            "/callbacks/about-to-start?page=alternativeServicePayment");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-confirm-payment-bailiff.json"
            )));
    }

    @Test
    public void shouldSetStateToAwaitingBailiffReferralWhenServiceTypeIsBailiff() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST);

        caseData.put("alternativeServiceType", "bailiff");

        Response response = triggerCallback(caseData, CASEWORKER_SERVICE_PAYMENT,
            "/callbacks/about-to-submit?page=AltPaymentSummary");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString(),
            json -> json.inPath("state").isEqualTo(AwaitingBailiffReferral.getName())
        );
    }

    @Test
    public void shouldSetStateToAwaitingServiceConsiderationWhenServiceTypeIsNotBailiff() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST);

        caseData.put("alternativeServiceType", "deemed");

        Response response = triggerCallback(caseData, CASEWORKER_SERVICE_PAYMENT,
            "/callbacks/about-to-submit?page=AltPaymentSummary");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString(),
            json -> json.inPath("state").isEqualTo(AwaitingServiceConsideration.getName())
        );
    }
}
