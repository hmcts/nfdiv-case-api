package uk.gov.hmcts.divorce.common;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleFinalOrder.SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest

public class SwitchedToSoleFinalOrderFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-app1-apply-for-final-order.json";

    @Test
    public void shouldSendSwitchToSoleFoNotifications() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, SWITCH_TO_SOLE_FO, SUBMITTED_URL, FinalOrderRequested);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendWelshSwitchToSoleFoNotificationsIfLanguagePreferenceIsWelsh() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("applicant1LanguagePreferenceWelsh", "Yes");
        caseData.put("applicant2LanguagePreferenceWelsh", "Yes");

        final Response response = triggerCallback(caseData, SWITCH_TO_SOLE_FO, SUBMITTED_URL, FinalOrderRequested);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
