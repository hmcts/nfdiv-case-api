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
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemPronounceCaseFT extends FunctionalTestSuite {

    private static final String SOLE_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-pronounce-case.json";
    private static final String SOLE_RESPONSE =
        "classpath:responses/response-system-pronounce-case.json";

    private static final String JOINT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-pronounce-case-joint-application.json";
    private static final String JOINT_RESPONSE =
        "classpath:responses/response-system-pronounce-case-joint.json";

    @Test
    public void shouldGenerateCOGrantedDocAndSendPronouncementNotificationToApplicant() throws IOException {
        Map<String, Object> request = caseData(SOLE_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(SOLE_RESPONSE)));
    }

    @Test
    public void shouldGenerateCOGrantedDocAndSendPronouncementNotificationToJointApplicant() throws IOException {
        Map<String, Object> request = caseData(JOINT_REQUEST);

        Response response = triggerCallback(request, SYSTEM_PRONOUNCE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(JOINT_RESPONSE)));
    }
}
