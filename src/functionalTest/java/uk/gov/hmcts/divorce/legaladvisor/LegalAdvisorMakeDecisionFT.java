package uk.gov.hmcts.divorce.legaladvisor;

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
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CO_REFUSAL_ORDER_WITH_AMENDMENTS_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CO_REFUSAL_ORDER_WITH_MORE_INFO_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class LegalAdvisorMakeDecisionFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision.json";
    private static final String JOINT_WELSH_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision-joint-welsh.json";
    private static final String CO_MORE_INFO_RESPONSE = "classpath:responses/response-legal-advisor-make-decision-co-more-info.json";
    private static final String CO_REJECTED_RESPONSE = "classpath:responses/response-legal-advisor-make-decision-co-rejected.json";
    private static final String REQUEST_JOINT_APPS_REPRESENTED
        = "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision-joint-apps-represented.json";
    private static final String CO_REJECTED_JOINT_APPS_REPRESENTED_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-rejected-joint-apps-represented.json";
    private static final String CO_REJECTED_JOINT_APPS_REPRESENTED_MORE_INFO_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-rejected-more-info-joint-apps-represented.json";

    private static final String CO_MORE_INFO_MID_EVENT_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-more-info-mid-event.json";
    private static final String CO_REJECTED_MID_EVENT_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-rejected-mid-event.json";

    @Test
    public void shouldSendEmailToApp1SolicitorAndGenerateRefusalOrderWhenMoreInfoSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_MORE_INFO_RESPONSE)));
    }

    @Test
    public void shouldGenerateRefusalOrderWhenRejectedSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coRefusalDecision", "reject");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_RESPONSE)));
    }

    @Test
    public void shouldSendWelshNotificationsIfJointConditionalOrderNotGranted() throws IOException {
        Map<String, Object> request = caseData(JOINT_WELSH_REQUEST);
        request.put("coRefusalDecision", "moreInfo");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailToBothApplicantSolicitorsAndGenerateRefusalOrderWhenRejectedSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST_JOINT_APPS_REPRESENTED);

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_JOINT_APPS_REPRESENTED_RESPONSE)));
    }

    @Test
    public void shouldSendEmailToBothApplicantSolicitorsAndGenerateRefusalOrderWhenMoreInfoSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST_JOINT_APPS_REPRESENTED);
        request.put("coRefusalDecision", "moreInfo");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_JOINT_APPS_REPRESENTED_MORE_INFO_RESPONSE)));
    }

    @Test
    public void midEventShouldGenerateRefusalOrderWhenMoreInfoSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coRefusalDecision", "moreInfo");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, CO_REFUSAL_ORDER_WITH_MORE_INFO_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_MORE_INFO_MID_EVENT_RESPONSE)));
    }

    @Test
    public void midEventShouldGenerateRefusalOrderWhenAmendApplicationSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coRefusalDecision", "reject");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, CO_REFUSAL_ORDER_WITH_AMENDMENTS_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_MID_EVENT_RESPONSE)));
    }
}
