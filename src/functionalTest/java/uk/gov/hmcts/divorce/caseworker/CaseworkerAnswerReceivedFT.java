package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAnswerReceived.CASEWORKER_ADD_ANSWER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CaseworkerAnswerReceivedFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-caseworker-answer-received-submitted.json";

    @Test
    public void shouldUpdateCaseDataWithNotesWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_ADD_ANSWER, SUBMITTED_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json("{}"));
    }
}
