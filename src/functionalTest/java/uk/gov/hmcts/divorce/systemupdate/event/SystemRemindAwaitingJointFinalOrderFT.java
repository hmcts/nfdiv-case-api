package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindAwaitingJointFinalOrder.SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class SystemRemindAwaitingJointFinalOrderFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-remind-awaiting-joint-final-order.json";

    @Test
    public void shouldPassValidationAndSendReminderEmailToApplicants() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThat(new JSONObject(response.asString()).getJSONObject("data").get("applicantsRemindedAwaitingJointFinalOrder"))
            .isEqualTo("Yes");
    }
}
