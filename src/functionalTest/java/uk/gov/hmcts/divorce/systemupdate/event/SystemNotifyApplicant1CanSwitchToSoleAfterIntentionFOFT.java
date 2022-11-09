package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicant1CanSwitchToSoleAfterIntentionFO.SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class SystemNotifyApplicant1CanSwitchToSoleAfterIntentionFOFT extends FunctionalTestSuite {

    private static final String SOLICITOR_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-notify-applicant1-can-switch-to-sole-after-intention-fo.json";

    @Test
    public void shouldSendEmailToApplicant1SolicitorWhenIntendedToSwitchToSole() throws IOException {
        Map<String, Object> request = caseData(SOLICITOR_REQUEST);

        Response response = triggerCallback(request, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
