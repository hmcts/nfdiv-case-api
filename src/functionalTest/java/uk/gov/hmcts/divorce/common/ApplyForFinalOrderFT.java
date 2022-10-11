package uk.gov.hmcts.divorce.common;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.time.LocalDate;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.common.event.ApplyForFinalOrder.FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class ApplyForFinalOrderFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-app1-apply-for-final-order.json";
    private static final String RESPONSE = "classpath:responses/response-app1-apply-for-final-order.json";

    private static final String REQUEST_JOINT_SOLICITOR
        = "classpath:request/casedata/ccd-callback-casedata-app1-apply-for-final-order-joint-solicitor.json";
    private static final String RESPONSE_JOINT_SOLICITOR
        = "classpath:responses/response-app1-apply-for-final-order-joint-solicitor.json";

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForSoleCase() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("dateFinalOrderNoLongerEligible", LocalDate.now().plusDays(1).toString());

        final Response response = triggerCallback(caseData, FINAL_ORDER_REQUESTED, ABOUT_TO_SUBMIT_URL, AwaitingFinalOrder);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForJointSolicitorCase() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST_JOINT_SOLICITOR);
        caseData.put("dateFinalOrderNoLongerEligible", LocalDate.now().plusDays(1).toString());

        final Response response = triggerCallback(caseData, FINAL_ORDER_REQUESTED, ABOUT_TO_SUBMIT_URL, AwaitingFinalOrder);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(RESPONSE_JOINT_SOLICITOR)));
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForJointSolicitorCaseSecondInTime() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST_JOINT_SOLICITOR);
        caseData.put("applicant1AppliedForFinalOrderFirst", "No");
        caseData.put("applicant2AppliedForFinalOrderFirst", "Yes");
        caseData.put("dateFinalOrderNoLongerEligible", LocalDate.now().plusDays(1).toString());

        final Response response = triggerCallback(caseData, FINAL_ORDER_REQUESTED, ABOUT_TO_SUBMIT_URL, AwaitingJointFinalOrder);

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(RESPONSE_JOINT_SOLICITOR));
        jsonDocument.set("state", "FinalOrderRequested");
        jsonDocument.set("data.applicant1AppliedForFinalOrderFirst", "No");
        jsonDocument.set("data.applicant2AppliedForFinalOrderFirst", "Yes");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    public void shouldMoveStateToFinalOrderRequestedWhenBothApplicantsHaveAppliedForFOInJointCitizenCase()
        throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("applicationType", "jointApplication");
        caseData.put("applicant2Email", "app2@email.com");
        caseData.put("dateFinalOrderNoLongerEligible", LocalDate.now().plusDays(30).toString());
        caseData.put("applicant1AppliedForFinalOrderFirst", "Yes");
        caseData.put("applicant2AppliedForFinalOrderFirst", "No");

        final Response response = triggerCallback(caseData, FINAL_ORDER_REQUESTED, ABOUT_TO_SUBMIT_URL, AwaitingJointFinalOrder);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(RESPONSE));
        jsonDocument.set("data.applicationType", "jointApplication");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());
    }
}
