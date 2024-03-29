package uk.gov.hmcts.divorce.solicitor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.common.event.Applicant2SolicitorApplyForFinalOrder.FINAL_ORDER_REQUESTED_APP2_SOL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP2_SOL_FO_PAYMENT_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class App2SolicitorFinalOrderPaymentMidEventCallbackFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-app2-sol-apply-for-final-order.json";
    private static final String LANGUAGE_PREFERENCE_WELSH = "languagePreferenceWelsh";

    @Test
    public void shouldRetrieveAndSetPbaNumbersWhenMidEventCallbackIsInvoked() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST);
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO);
        caseData.put(FINANCIAL_ORDER, NO);

        Response response = triggerCallback(caseData, FINAL_ORDER_REQUESTED_APP2_SOL, APP2_SOL_FO_PAYMENT_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(TREATING_NULL_AS_ABSENT, IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-solicitor-final-order-payment-mid-event.json"
            )));
    }
}
