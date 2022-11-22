package uk.gov.hmcts.divorce.solicitor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorIntendsSwitchToSoleFo.APPLICANT_2_INTENDS_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class Applicant2SolicitorIntendsSwitchToSoleFoFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-applicant-2-solicitor-intend-switch-to-sole-fo.json";

    @Test
    public void shouldSendEmailToApplicant1SolicitorWhenRepresented() throws IOException {

        final Map<String, Object> caseData = caseData(REQUEST);

        Response response = triggerCallback(caseData, APPLICANT_2_INTENDS_TO_SWITCH_TO_SOLE_FO, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
