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
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerExpediteFinalOrder.CASEWORKER_EXPEDITE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerExpediteFinalOrderFT extends FunctionalTestSuite {

    public static final String RESPONSE_CASEWORKER_EXPEDITE_FINAL_ORDER_JSON =
        "classpath:responses/response-caseworker-expedite-final-order.json";
    public static final String REQUEST_CASEWORKER_EXPEDITE_FINAL_ORDER_JSON =
        "classpath:request/casedata/ccd-callback-caseworker-expedite-final-order.json";

    public static final String RESPONSE_CASEWORKER_EXPEDITE_FINAL_ORDER_OFFLINE_JSON =
        "classpath:responses/response-caseworker-expedite-final-order-offline.json";
    public static final String REQUEST_CASEWORKER_EXPEDITE_FINAL_ORDER_OFFLINE_JSON =
        "classpath:request/casedata/ccd-callback-caseworker-expedite-final-order-offline.json";

    private static final String REQUEST_CASEWORKER_EXPEDITE_FINAL_ORDER_SOLICITOR_JSON =
        "classpath:request/casedata/ccd-callback-caseworker-expedite-final-order-solicitor.json";

    @Test
    public void shouldGenerateGrantFinalOrderDocumentAndUpdateCaseDataWhenAboutToSubmitCallbackIsInvokedForDivorce() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_CASEWORKER_EXPEDITE_FINAL_ORDER_JSON);
        final Response response = triggerCallback(caseData, CASEWORKER_EXPEDITE_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);
        caseData.put("finalOrderSwitchedToSole", YES);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(RESPONSE_CASEWORKER_EXPEDITE_FINAL_ORDER_JSON)));
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentInWelshAndUpdateCaseDataWhenAboutToSubmitCallbackIsInvokedForDivorce()
        throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_CASEWORKER_EXPEDITE_FINAL_ORDER_JSON);
        caseData.put("applicant1LanguagePreferenceWelsh", "Yes");
        caseData.put("finalOrderSwitchedToSole", YES);

        final Response response = triggerCallback(caseData, CASEWORKER_EXPEDITE_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentAndUpdateCaseDataWhenAboutToSubmitCallbackIsInvokedForCP() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_CASEWORKER_EXPEDITE_FINAL_ORDER_JSON);
        caseData.put("divorceOrDissolution", "dissolution");
        caseData.put("finalOrderSwitchedToSole", YES);

        final Response response = triggerCallback(caseData, CASEWORKER_EXPEDITE_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(RESPONSE_CASEWORKER_EXPEDITE_FINAL_ORDER_JSON));
        jsonDocument.set("data.divorceOrDissolution", "dissolution");

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentAndCoverLetterWhenAboutToSubmitCallbackIsInvokedForOfflineCase() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_CASEWORKER_EXPEDITE_FINAL_ORDER_OFFLINE_JSON);
        caseData.put("finalOrderSwitchedToSole", YES);
        final Response response = triggerCallback(caseData, CASEWORKER_EXPEDITE_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE_CASEWORKER_EXPEDITE_FINAL_ORDER_OFFLINE_JSON)));
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentInWelshAndUpdateCaseDataWhenAboutToSubmitCallbackIsInvokedForCP() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_CASEWORKER_EXPEDITE_FINAL_ORDER_JSON);

        caseData.put("divorceOrDissolution", "dissolution");
        caseData.put("applicant1LanguagePreferenceWelsh", "Yes");
        caseData.put("finalOrderSwitchedToSole", YES);

        final Response response = triggerCallback(caseData, CASEWORKER_EXPEDITE_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
