package uk.gov.hmcts.divorce.citizen;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitApplication.CITIZEN_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CitizenSubmitApplicationFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-applicant1-statement-of-truth.json";
    private static final String REQUEST_JOINT =
        "classpath:request/casedata/ccd-callback-casedata-applicant1-joint-application.json";
    private static final String REQUEST_JOINT_HWF =
        "classpath:request/casedata/ccd-callback-casedata-joint-application-help-with-fees-awaiting-documents.json";
    private static final String REQUEST_WELSH_EMAIL_NOTIFICATION =
        "classpath:request/casedata/ccd-callback-casedata-joint-application-welsh-email-notification.json";
    private static final String RESPONSE = "classpath:responses/response-applicant1-statement-of-truth.json";
    private static final String RESPONSE_JOINT = "classpath:responses/response-applicant1-joint-application.json";
    private static final String RESPONSE_JOINT_HWF =
        "classpath:responses/response-joint-application-help-with-fees-awaiting-documents.json";
    private static final String RESPONSE_HWF = "classpath:responses/response-applicant1-help-with-fees.json";

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValid() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("marriageDate", LocalDate.now().minus(1, YEARS).minus(1, DAYS));

        Response response = triggerCallback(request, CITIZEN_SUBMIT, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // marriageDate and payments.id are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndGiveSuccessWhenApplicant1HelpWithFeesApplied() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("marriageDate", LocalDate.now().minus(1, YEARS).minus(1, DAYS));
        request.put("applicant1HWFNeedHelp", "YES");

        Response response = triggerCallback(request, CITIZEN_SUBMIT, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // marriageDate and payments.id are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse(RESPONSE_HWF));
    }

    @Test
    public void shouldPassValidationAndGiveSuccessWhenApplicant1AppliesAsJointApplication() throws IOException {
        Map<String, Object> request = caseData(REQUEST_JOINT);
        request.put("marriageDate", LocalDate.now().minus(1, YEARS).minus(1, DAYS));

        Response response = triggerCallback(request, CITIZEN_SUBMIT, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // marriageDate and payments.id are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse(RESPONSE_JOINT));
    }

    @Test
    public void shouldPassValidationAndSendEmailsToApplicant1AndApplicant2() throws IOException {
        Map<String, Object> request = caseData(REQUEST_JOINT_HWF);

        Response response = triggerCallback(request, CITIZEN_SUBMIT, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE_JOINT_HWF)));
    }

    @Test
    public void shouldPassValidationAndSendWelshEmailsToApplicant1AndApplicant2() throws IOException {
        Map<String, Object> request = caseData(REQUEST_WELSH_EMAIL_NOTIFICATION);

        Response response = triggerCallback(request, CITIZEN_SUBMIT, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
