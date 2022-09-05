package uk.gov.hmcts.divorce.citizen;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CitizenPaymentMadeFT extends FunctionalTestSuite {

    private static final String PAYMENT_IN_PROGRESS_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-payment-in-progress.json";
    private static final String PAYMENT_IN_PROGRESS_RESPONSE =
        "classpath:responses/response-payment-in-progress.json";

    private static final String PAYMENT_CANCELLED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-payment-cancelled.json";
    private static final String PAYMENT_CANCELLED_RESPONSE =
        "classpath:responses/response-payment-cancelled.json";

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-payment-made.json";
    private static final String RESPONSE =
        "classpath:responses/response-payment-made.json";

    public static final String JOINT_AWAITING_DOCUMENTS_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-joint-application-payment-made-awaiting-documents.json";
    public static final String JOINT_AWAITING_DOCUMENTS_RESPONSE =
        "classpath:responses/response-joint-application-payment-made-awaiting-documents.json";

    private static final String AWAITING_DOCUMENTS_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-payment-made-awaiting-documents.json";
    private static final String AWAITING_DOCUMENTS_RESPONSE =
        "classpath:responses/response-payment-made-awaiting-documents.json";

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValidWaitingForPayment() throws IOException {
        Map<String, Object> request = caseData(PAYMENT_IN_PROGRESS_REQUEST);
        Response response = triggerCallback(request, CITIZEN_PAYMENT_MADE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(PAYMENT_IN_PROGRESS_RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValidLastPaymentCancelled() throws IOException {
        Map<String, Object> request = caseData(PAYMENT_CANCELLED_REQUEST);
        Response response = triggerCallback(request, CITIZEN_PAYMENT_MADE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(PAYMENT_CANCELLED_RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValidAndSendEmailToApplicant1AndApplicant2() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        Response response = triggerCallback(request, CITIZEN_PAYMENT_MADE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValidAndAwaitingDocument() throws IOException {
        Map<String, Object> request = caseData(AWAITING_DOCUMENTS_REQUEST);
        Response response = triggerCallback(request, CITIZEN_PAYMENT_MADE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(AWAITING_DOCUMENTS_RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndGiveSuccessWhenSoleCaseAndApplicantWantToServePapersAlternatively() throws IOException {
        Map<String, Object> request = caseData(AWAITING_DOCUMENTS_REQUEST);
        request.put("applicant1WantsToHavePapersServedAnotherWay", "Yes");
        request.put("applicant1KnowsApplicant2Address", "No");
        request.remove("applicant1CannotUploadSupportingDocument");

        Response response = triggerCallback(request, CITIZEN_PAYMENT_MADE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(AWAITING_DOCUMENTS_RESPONSE));
        jsonDocument.delete("data.applicant1CannotUploadSupportingDocument");

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValidAndAwaitingDocumentJointApplication() throws IOException {
        Map<String, Object> request = caseData(JOINT_AWAITING_DOCUMENTS_REQUEST);
        Response response = triggerCallback(request, CITIZEN_PAYMENT_MADE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(JOINT_AWAITING_DOCUMENTS_RESPONSE)));
    }
}
