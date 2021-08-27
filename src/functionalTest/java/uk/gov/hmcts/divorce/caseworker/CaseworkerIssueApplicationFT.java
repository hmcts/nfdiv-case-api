package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerIssueApplicationFT extends FunctionalTestSuite {

    private static final String SOLICITOR_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-issue-application-about-to-submit.json";
    private static final String SOLICITOR_RESPONSE =
        "classpath:responses/response-caseworker-issue-application-about-to-submit.json";
    private static final String CITIZEN_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-issue-citizen-application-about-to-submit.json";
    private static final String CITIZEN_RESPONSE =
        "classpath:responses/response-caseworker-issue-citizen-application-about-to-submit.json";

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForSolicitorApplication() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // document_url and document_binary_url are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE
            )));
    }

    @Test
    public void shouldUpdateCaseDataAndSendEmailsWhenAboutToSubmitCallbackIsSuccessfulForCitizenApplication() throws Exception {
        final Map<String, Object> caseData = caseData(CITIZEN_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // document_url and document_binary_url are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                CITIZEN_RESPONSE
            )));
    }
}
