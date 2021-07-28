package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejected.CASEWORKER_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class CaseworkerRejectedTest extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-caseworker-reject-application-about-to-submit.json";

    @Test
    public void shouldSetPreviousStateWhenAboutToStartCallbackIsSuccessful() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_REJECTED, ABOUT_TO_SUBMIT_URL, Submitted);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString(),
            json -> json.inPath("data.previousState").isEqualTo(Submitted.getName())
        );
    }
}
