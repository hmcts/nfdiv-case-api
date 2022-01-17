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
    private static final String RESPONSE = "classpath:responses/response-system-update-case-with-court-hearing.json";

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
}
