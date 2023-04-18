package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemUpdateCaseWithCourtHearingFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-update-case-with-court-hearing.json";
    private static final String JOINT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-update-case-with-court-hearing-joint.json";
    private static final String REPRESENTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-update-case-with-court-hearing-represented.json";
    private static final String RESPONSE = "classpath:responses/response-system-update-case-with-court-hearing.json";
    private static final String RESPONSE_OFFLINE = "classpath:responses/response-system-update-case-with-court-hearing-offline.json";
    private static final String RESPONSE_OFFLINE_WITH_PRIVATE_CONTACT =
        "classpath:responses/response-system-update-case-with-court-hearing-offline-with-private-contact.json";

    @Test
    public void shouldSendEmailsToApplicantAndRespondentAndCreateCertificateOfEntitlementDocument() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, SYSTEM_UPDATE_CASE_COURT_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldSendEmailsToApplicantsAndCreateCertificateOfEntitlementDocument() throws IOException {
        Map<String, Object> request = caseData(JOINT_REQUEST);

        Response response = triggerCallback(request, SYSTEM_UPDATE_CASE_COURT_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldSendEmailsToApplicantAndRespondentSolicitorsAndCreateCertificateOfEntitlementDocument()
        throws IOException {

        Map<String, Object> request = caseData(REPRESENTED_REQUEST);

        Response response = triggerCallback(request, SYSTEM_UPDATE_CASE_COURT_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldSendLettersToApplicantsAndCreateCertificateOfEntitlementDocumentAndCoverLetter()
        throws IOException {

        Map<String, Object> request = caseData(JOINT_REQUEST);
        request.put("applicant1Offline", YES);
        request.put("applicant2Offline", YES);

        Response response = triggerCallback(request, SYSTEM_UPDATE_CASE_COURT_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE_OFFLINE)));
    }

    @Test
    public void shouldSendLettersToApplicantsAndCreateCertificateOfEntitlementDocumentAndCoverLetterWhenBothContactsArePrivate()
        throws IOException {

        Map<String, Object> request = caseData(JOINT_REQUEST);
        request.put("applicant1Offline", YES);
        request.put("applicant2Offline", YES);
        request.put("applicant1ContactDetailsType", "private");
        request.put("applicant2ContactDetailsType", "private");

        Response response = triggerCallback(request, SYSTEM_UPDATE_CASE_COURT_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE_OFFLINE_WITH_PRIVATE_CONTACT)));
    }

    @Test
    public void shouldSendLettersToOfflineRespondentInSoleApplicationAndCreateCertificateOfEntitlementDocumentAndCoverLetter()
        throws IOException {

        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant1Offline", YES);
        request.put("applicant2Offline", YES);

        Response response = triggerCallback(request, SYSTEM_UPDATE_CASE_COURT_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldSendLettersToOfflineApplicantsInSoleJSApplicationAndCreateCertificateOfEntitlementDocumentAndCoverLetter()
        throws IOException {

        Map<String, Object> request = caseData(REQUEST);
        request.put("supplementaryCaseType", JUDICIAL_SEPARATION);
        request.put("applicant1Offline", YES);
        request.put("applicant2Offline", YES);

        Response response = triggerCallback(request, SYSTEM_UPDATE_CASE_COURT_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldSendLettersToOfflineApplicantSolicitorInJointJSApplicationAndCreateCertificateOfEntitlementDocumentAndCoverLetter()
        throws IOException {

        Map<String, Object> request = caseData(JOINT_REQUEST);
        request.put("supplementaryCaseType", JUDICIAL_SEPARATION);
        request.put("applicant1SolicitorRepresented", YES);
        request.put("applicant1Offline", YES);
        request.put("applicant2Offline", YES);

        Response response = triggerCallback(request, SYSTEM_UPDATE_CASE_COURT_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE_OFFLINE)));
    }
}
