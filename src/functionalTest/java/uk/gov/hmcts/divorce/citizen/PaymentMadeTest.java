package uk.gov.hmcts.divorce.citizen;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.citizen.event.PaymentMade.PAYMENT_MADE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_SUBMIT_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedCcdCallbackResponse;

@SpringBootTest
public class PaymentMadeTest extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-payment-made.json";
    private static final String RESPONSE = "classpath:responses/ccd-callback-casedata-payment-made.json";

    private static final String AWAITING_DOCUMENTS_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-payment-made-awaiting-documents.json";
    private static final String AWAITING_DOCUMENTS_RESPONSE =
        "classpath:responses/ccd-callback-casedata-payment-made-awaiting-documents.json";

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValid() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        Response response = triggerCallback(request, PAYMENT_MADE, ABOUT_TO_SUBMIT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString()).isEqualTo(json(expectedCcdCallbackResponse(RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValidAndAwaitingDocument() throws IOException {
        Map<String, Object> request = caseData(AWAITING_DOCUMENTS_REQUEST);
        Response response = triggerCallback(request, PAYMENT_MADE, ABOUT_TO_SUBMIT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(json(response.asString())).isEqualTo(json(expectedCcdCallbackResponse(AWAITING_DOCUMENTS_RESPONSE)));
    }

}
