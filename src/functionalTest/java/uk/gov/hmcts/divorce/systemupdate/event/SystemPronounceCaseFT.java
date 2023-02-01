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
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemPronounceCaseFT extends FunctionalTestSuite {

    private static final String SOLE_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-pronounce-case.json";
    private static final String SOLE_RESPONSE =
        "classpath:responses/response-system-pronounce-case.json";

    private static final String SOLE_REPRESENTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-pronounce-case-applicant-represented.json";
    private static final String SOLE_REPRESENTED_RESPONSE =
        "classpath:responses/response-system-pronounce-case-applicant-represented.json";

    private static final String JOINT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-pronounce-case-joint-application.json";
    private static final String JOINT_RESPONSE =
        "classpath:responses/response-system-pronounce-case-joint.json";

    private static final String OFFLINE_NOT_REPRESENTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-pronounce-case-offline.json";
    private static final String OFFLINE_NOT_REPRESENTED_RESPONSE =
        "classpath:responses/response-system-pronounce-case-offline.json";
    private static final String JUDICIAL_SEPARATION_REPRESENTED_RESPONSE =
        "classpath:responses/response-system-pronounce-case-judical-separation-represented.json";
    private static final String OFFLINE_WITH_PRIVATE_CONTACT_RESPONSE =
        "classpath:responses/response-system-pronounce-case-offline-with-private-contract.json";

    private static final String OFFLINE_REPRESENTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-pronounce-case-offline-represented.json";
    private static final String OFFLINE_REPRESENTED_RESPONSE =
        "classpath:responses/response-system-pronounce-case-offline-represented.json";

    @Test
    public void shouldGenerateCOGrantedDocForSoleCase() throws IOException {
        Map<String, Object> request = caseData(SOLE_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(SOLE_RESPONSE)));
    }

    @Test
    public void shouldSendPronouncementNotificationToSoleApplicant() throws IOException {
        Map<String, Object> request = caseData(SOLE_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldGenerateCOGrantedDocForJointCase() throws IOException {
        Map<String, Object> request = caseData(JOINT_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(JOINT_RESPONSE)));
    }

    @Test
    public void shouldSendPronouncementNotificationToJointApplicant() throws IOException {
        Map<String, Object> request = caseData(JOINT_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldGenerateCOGrantedDocForSoleCaseAndApplicantRepresented() throws IOException {
        Map<String, Object> request = caseData(SOLE_REPRESENTED_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(SOLE_REPRESENTED_RESPONSE)));
    }

    @Test
    public void shouldSendPronouncementNotificationToSoleApplicantSolicitor() throws IOException {
        Map<String, Object> request = caseData(SOLE_REPRESENTED_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldGenerateCOGrantedDocAndCoversheetAndSendPronouncementLettersToApplicants()
        throws IOException {

        Map<String, Object> request = caseData(OFFLINE_NOT_REPRESENTED_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(OFFLINE_NOT_REPRESENTED_RESPONSE)));
    }

    @Test
    public void shouldGenerateCOGrantedDocAndCoversheetAndSendPronouncementLettersToApplicantSolicitorsWhenRepresented()
        throws IOException {

        Map<String, Object> request = caseData(OFFLINE_REPRESENTED_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(OFFLINE_REPRESENTED_RESPONSE)));
    }

    @Test
    public void shouldGenerateCOGrantedDocAndCoversheetAndSendPronouncementLettersToApplicantsWhenContactIsPrivate()
        throws IOException {

        Map<String, Object> request = caseData(OFFLINE_NOT_REPRESENTED_REQUEST);
        request.put("applicant2ContactDetailsType", "private");

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        System.out.println(response.asString());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(OFFLINE_WITH_PRIVATE_CONTACT_RESPONSE)));
    }

    @Test
    public void shouldGenerateCOGrantedDocAndCoversheetAndSendPronouncementLettersToApplicantsInJudicialSeparation()
        throws IOException {

        Map<String, Object> request = caseData(OFFLINE_NOT_REPRESENTED_REQUEST);
        request.put("isJudicialSeparation", "Yes");

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(JUDICIAL_SEPARATION_REPRESENTED_RESPONSE)));
    }
}
