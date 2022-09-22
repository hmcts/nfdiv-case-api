package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAwaitingFinalOrder.SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class SystemProgressCaseToAwaitingFinalOrderFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-progress-case-to-awaiting-final-order.json";
    private static final String JOINT_REPRESENTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-progress-case-to-awaiting-final-order-joint-represented.json";

    private static final String WELSH_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-progress-case-to-awaiting-final-order-welsh.json";

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
    public void shouldPassValidationAndSendLettersToOfflineApplicants() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.remove("applicant1Email");
        request.remove("applicant2Email");
        request.put("applicant1Offline", "Yes");
        request.put("applicant2Offline", "Yes");

        Response response = triggerCallback(request, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
