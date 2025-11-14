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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerReissueApplicationFT extends FunctionalTestSuite {

    private static final String SOLICITOR_REQUEST_DIGITAL_AOS =
        "classpath:request/casedata/ccd-callback-caseworker-reissue-digital-aos-application-about-to-submit.json";
    private static final String SOLICITOR_RESPONSE_DIGITAL_AOS =
        "classpath:responses/response-caseworker-reissue-application-about-to-submit.json";

    private static final String SOLICITOR_RESPONSE_DIGITAL_AOS_WITH_D10 =
        "classpath:responses/response-caseworker-reissue-application-with-d10-about-to-submit.json";

    private static final String SOLICITOR_REQUEST_OFFLINE_AOS =
        "classpath:request/casedata/ccd-callback-caseworker-reissue-offline-aos-application-about-to-submit.json";
    private static final String SOLICITOR_RESPONSE_OFFLINE_AOS =
        "classpath:responses/response-caseworker-offline-aos-application-about-to-submit.json";

    private static final String SOLICITOR_REQUEST_REISSUE_CASE =
        "classpath:request/casedata/ccd-callback-caseworker-reissue-case-type-application-about-to-submit.json";
    private static final String SOLICITOR_RESPONSE_REISSUE_TYPE =
        "classpath:responses/response-caseworker-reissue-application-about-to-submit-reissue-case.json";

    private static final String RE_ISSUE_SOLE_APPLICATION_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-reissue-sole-application-applicant-represented-about-to-submit.json";
    private static final String RE_ISSUE_SOLE_APPLICATION_RESPONSE =
        "classpath:responses/response-caseworker-reissue-sole-application-applicant-represented-about-to-submit.json";

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldGenerateRespondentAosAndSendEmailToApplicantAndRespondentSolicitorWhenReissueTypeIsDigitalAos() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_DIGITAL_AOS);
        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE_DIGITAL_AOS
            )));
    }

    @Test
    public void shouldSendNotificationsToApplicantAndRespondentSolicitorWhenReissueTypeIsDigitalAos() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_DIGITAL_AOS);
        caseData.put("previousReissueOption", "digitalAos");
        caseData.put("dueDate", "2022-01-15");
        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldGenerateRespondentAosAndD10DocumentAndSendEmailToApplicantAndRespondentSolicitorWhenReissueTypeIsDigitalAos()
        throws Exception {

        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_DIGITAL_AOS);
        caseData.put("serviceMethod", SOLICITOR_SERVICE);
        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE_DIGITAL_AOS_WITH_D10
            )));
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldGenerateRespondentAosAndSentAosPackAndNotSendEmailNotificationWhenReissueTypeIsOfflineAos() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_OFFLINE_AOS);
        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE_OFFLINE_AOS
            )));
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldGenerateRespondentAosAndMiniApplicationAndSentAosPackAndSendEmailNotificationWhenReissueTypeIsReissueCase()
        throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_REISSUE_CASE);
        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);
        caseData.put("serviceMethod", COURT_SERVICE);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE_REISSUE_TYPE
            )));
    }

    @Test
    public void shouldSendAosPackAndSendEmailNotificationWhenReissueTypeIsReissueCase()
        throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_REISSUE_CASE);
        caseData.put("serviceMethod", COURT_SERVICE);
        caseData.put("previousReissueOption", "reissueCase");
        caseData.put("dueDate", "2022-01-15");

        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendReIssueEmailNotificationWhenSoleApplicationAndApplicantIsRepresented() throws Exception {
        final Map<String, Object> caseData = caseData(RE_ISSUE_SOLE_APPLICATION_REQUEST);
        caseData.put("serviceMethod", COURT_SERVICE);
        caseData.put("previousReissueOption", "digitalAos");
        caseData.put("dueDate", "2022-01-15");
        caseData.put("accessCode", ACCESS_CODE);

        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, SUBMITTED_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldGenerateOfflineAOSRespondentNOPWhenSoleApplicationAndReissuedAsOfflineAOS() throws Exception {
        final Map<String, Object> caseData = caseData(RE_ISSUE_SOLE_APPLICATION_REQUEST);
        caseData.put("serviceMethod", COURT_SERVICE);
        caseData.put("reissueOption", "offlineAos");
        caseData.put("dueDate", "2022-01-15");
        caseData.put("accessCode", ACCESS_CODE);

        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldReIssueApplicationWhenSoleJudicialSeparationAndReissuedAsOfflineAOS() throws Exception {
        final Map<String, Object> caseData = caseData(RE_ISSUE_SOLE_APPLICATION_REQUEST);
        caseData.put("applicant1Represented", "No");
        caseData.put("supplementaryCaseType", JUDICIAL_SEPARATION);
        caseData.put("judicialSeparationReissueOption", "offlineAos");
        caseData.put("dueDate", "2022-01-15");
        caseData.put("accessCode", ACCESS_CODE);

        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldReIssueJudicialSeparationApplicationWhenSoleAppRepresentedAndReissuedAsOfflineAOS() throws Exception {
        final Map<String, Object> caseData = caseData(RE_ISSUE_SOLE_APPLICATION_REQUEST);
        caseData.put("supplementaryCaseType", JUDICIAL_SEPARATION);
        caseData.put("judicialSeparationReissueOption", "offlineAos");
        caseData.put("dueDate", "2022-01-15");
        caseData.put("accessCode", ACCESS_CODE);

        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
