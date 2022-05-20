package uk.gov.hmcts.divorce.legaladvisor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorGeneralConsideration.LEGAL_ADVISOR_GENERAL_CONSIDERATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class LegalAdvisorGeneralConsiderationFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-general-consideration.json";

    @Test
    public void shouldSendNotificationsToApplicantAndRespondentIfGeneralReferralRefused() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, LEGAL_ADVISOR_GENERAL_CONSIDERATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
