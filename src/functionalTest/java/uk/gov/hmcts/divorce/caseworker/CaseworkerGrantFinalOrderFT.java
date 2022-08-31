package uk.gov.hmcts.divorce.caseworker;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGrantFinalOrder.CASEWORKER_GRANT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerGrantFinalOrderFT extends FunctionalTestSuite {

    public static final String RESPONSE_CASEWORKER_GRANT_FINAL_ORDER_JSON =
        "classpath:responses/response-caseworker-grant-final-order.json";
    public static final String REQUEST_CASEWORKER_GRANT_FINAL_ORDER_JSON =
        "classpath:request/casedata/ccd-callback-caseworker-grant-final-order.json";

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-grant-final-order-about-to-submit.json";

    @Test
    public void shouldSendBothSolicitorsEmailsWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_GRANT_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentAndUpdateCaseDataWhenAboutToSubmitCallbackIsInvokedForDivorce() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_CASEWORKER_GRANT_FINAL_ORDER_JSON);

        final Response response = triggerCallback(caseData, CASEWORKER_GRANT_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(RESPONSE_CASEWORKER_GRANT_FINAL_ORDER_JSON)));
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentAndUpdateCaseDataWhenAboutToSubmitCallbackIsInvokedForCP() throws Exception {
        final Map<String, Object> caseData = caseData(
            REQUEST_CASEWORKER_GRANT_FINAL_ORDER_JSON);
        caseData.put("divorceOrDissolution", "dissolution");

        final Response response = triggerCallback(caseData, CASEWORKER_GRANT_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        DocumentContext jsonDocument = JsonPath.parse(
            expectedResponse(RESPONSE_CASEWORKER_GRANT_FINAL_ORDER_JSON));
        jsonDocument.set("data.divorceOrDissolution", "dissolution");

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());
    }
}
