package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyJointApplicantCanSwitchToSole.SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.CCD_DATE_TIME_FORMAT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemNotifyJointApplicantCanSwitchToSoleFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-notify-joint-applicant-can-switch-to-sole.json";
    private static final String RESPONSE = "classpath:responses/response-system-notify-joint-applicant-can-switch-to-sole.json";

    private static final LocalDate NOW = LocalDate.now();

    @Test
    public void shouldPassValidationAndSendEmailsToApplicant1() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coApplicant1IsSubmitted", "Yes");
        request.put("coApplicant1SubmittedDate", getFormattedDateTime(NOW.minusDays(15)));

        Response response = triggerCallback(request, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, ABOUT_TO_SUBMIT_URL,
            ConditionalOrderPending);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndSendEmailsToApplicant2() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coApplicant2IsSubmitted", "Yes");
        request.put("coApplicant2SubmittedDate", getFormattedDateTime(NOW.minusDays(15)));

        Response response = triggerCallback(request, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, ABOUT_TO_SUBMIT_URL,
            ConditionalOrderPending);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndSendEmailInWelshToApplicant1() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant1LanguagePreferenceWelsh", YES);
        request.put("coApplicant1IsSubmitted", "Yes");
        request.put("coApplicant1SubmittedDate", getFormattedDateTime(NOW.minusDays(15)));

        Response response = triggerCallback(request, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, ABOUT_TO_SUBMIT_URL,
            ConditionalOrderPending);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldPassValidationAndSendEmailInWelshToApplicant2() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant2LanguagePreferenceWelsh", YES);
        request.put("coApplicant2IsSubmitted", "Yes");
        request.put("coApplicant2SubmittedDate", getFormattedDateTime(NOW.minusDays(15)));

        Response response = triggerCallback(request, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, ABOUT_TO_SUBMIT_URL,
            ConditionalOrderPending);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    private String getFormattedDateTime(LocalDate localDate) {
        return localDate.atStartOfDay().format(CCD_DATE_TIME_FORMAT);
    }
}
