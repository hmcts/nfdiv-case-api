package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerIssueApplicationFT extends FunctionalTestSuite {

    private static final String SOLICITOR_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-issue-application-about-to-submit.json";
    private static final String SOLICITOR_RESPONSE =
        "classpath:responses/response-caseworker-issue-application-about-to-submit.json";
    private static final String SOLICITOR_RESPONSE_WITH_D10 =
        "classpath:responses/response-caseworker-issue-application-with-d10-about-to-submit.json";
    private static final String SOLE_CITIZEN_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-issue-sole-citizen-application-about-to-submit.json";
    private static final String SOLE_CITIZEN_RESPONSE =
        "classpath:responses/response-caseworker-issue-sole-citizen-application-about-to-submit.json";
    private static final String SOLE_CITIZEN_REQUEST_APP2_OVERSEAS =
        "classpath:request/casedata/ccd-callback-caseworker-issue-sole-citizen-application-app2-overseas-about-to-submit.json";
    private static final String SOLE_CITIZEN_RESPONSE_APP2_OVERSEAS =
        "classpath:responses/response-caseworker-issue-sole-citizen-application-app2-overseas-about-to-submit.json";
    private static final String JOINT_CITIZEN_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-issue-joint-citizen-application-about-to-submit.json";
    private static final String JOINT_CITIZEN_RESPONSE =
        "classpath:responses/response-caseworker-issue-joint-citizen-application-about-to-submit.json";

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForSolicitorApplication() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // document_url and document_binary_url are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE
            )));
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldUpdateCaseDataAndAddD10DocumentWhenAboutToSubmitCallbackIsSuccessfulForSolicitorApplication() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST);
        caseData.put("serviceMethod", SOLICITOR_SERVICE);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE_WITH_D10
            )));
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForSoleCitizenApplication() throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                SOLE_CITIZEN_RESPONSE
            )));
    }

    @Test
    public void shouldSendNotificationsWhenSubmittedCallbackIsSuccessfulForSoleCitizenApplication() throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST);
        caseData.put("accessCode", "123456");

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendNotificationsWhenSubmittedCallbackIsSuccessfulForSoleCitizenApplicationApp2Overseas() throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST_APP2_OVERSEAS);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForSoleCitizenApplicationApp2Overseas() throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST_APP2_OVERSEAS);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                SOLE_CITIZEN_RESPONSE_APP2_OVERSEAS
            )));
    }

    @Test
    public void shouldSendNotificationsWhenApplicant1LanguageIsWelsh() throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST);
        caseData.put("applicant1LanguagePreferenceWelsh", YES);
        caseData.put("applicant1ServiceMethod", COURT_SERVICE);
        caseData.put("accessCode", "123456");

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForJointCitizenApplication() throws Exception {
        final Map<String, Object> caseData = caseData(JOINT_CITIZEN_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                JOINT_CITIZEN_RESPONSE
            )));
    }

    @Test
    public void shouldSendNotificationsWhenSubmittedCallbackIsSuccessfulForJointCitizenApplication() throws Exception {
        final Map<String, Object> caseData = caseData(JOINT_CITIZEN_REQUEST);
        caseData.put("issueDate", "2022-06-20");

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForOfflineSoleJudicialSeparation() throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST);
        caseData.put("supplementaryCaseType", JUDICIAL_SEPARATION);
        caseData.remove("applicant1Email");
        caseData.remove("applicant2Email");
        caseData.put("applicant1Offline", "Yes");
        caseData.put("applicant2Offline", "Yes");

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForSoleCitizenApplicationWithJudicialSeparation()
        throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST);
        caseData.put("supplementaryCaseType", JUDICIAL_SEPARATION);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForWelshSoleCitizenApplicationWithJudicialSeparation()
        throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST);
        caseData.put("applicant2LanguagePreferenceWelsh", "Yes");
        caseData.put("supplementaryCaseType", JUDICIAL_SEPARATION);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForJointJudicialSeparation() throws Exception {
        final Map<String, Object> caseData = caseData(JOINT_CITIZEN_REQUEST);
        caseData.put("supplementaryCaseType", JUDICIAL_SEPARATION);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForSoleJudicialSeparationApp1Represented() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST);
        caseData.put("supplementaryCaseType", JUDICIAL_SEPARATION);
        caseData.remove("applicant1Email");
        caseData.remove("applicant2Email");
        caseData.put("applicant1Offline", "Yes");
        caseData.put("applicant2Offline", "Yes");

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForSoleCitizenApplicationWithJSPersonalService() throws Exception {
        final Map<String, Object> caseData = caseData(SOLE_CITIZEN_REQUEST);
        caseData.put("supplementaryCaseType", JUDICIAL_SEPARATION);
        caseData.put("serviceMethod", "personalService");

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
