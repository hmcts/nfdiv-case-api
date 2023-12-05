package uk.gov.hmcts.divorce.systemupdate.event;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAwaitingFinalOrder.SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemProgressCaseToAwaitingFinalOrderFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-progress-case-to-awaiting-final-order.json";
    private static final String JOINT_REPRESENTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-progress-case-to-awaiting-final-order-joint-represented.json";
    private static final String WELSH_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-progress-case-to-awaiting-final-order-welsh.json";

    private static final String RESPONSE = "classpath:responses/response-system-progress-case-to-awaiting-final-order-offline.json";

    @Test
    public void shouldPassValidationAndSendEmailsToApplicantAndRespondent() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldPassValidationAndSendEmailsToApplicantAndRespondentWithLanguagePreferenceWelsh() throws IOException {
        Map<String, Object> request = caseData(WELSH_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldPassValidationAndSendEmailsToJointApplicantSolicitors() throws IOException {
        Map<String, Object> request = caseData(JOINT_REPRESENTED_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldGenerateLettersForOfflineApplicants() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant1Offline", YES);
        request.put("applicant2Offline", YES);
        request.put("applicant2Email", "");

        Response response = triggerCallback(request, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }
}
