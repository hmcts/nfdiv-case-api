package uk.gov.hmcts.divorce.citizen;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSaveAndClose.CITIZEN_SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CitizenSaveAndCloseTest extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";

    @Test
    public void shouldSendEmailWhenAllTemplateParamsAreValid() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        Response response = triggerCallback(request, CITIZEN_SAVE_AND_CLOSE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendWelshEmailWhenAllTemplateParamsAreValid() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("languagePreferenceWelsh", "YES");
        Response response = triggerCallback(request, SAVE_AND_CLOSE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

}
