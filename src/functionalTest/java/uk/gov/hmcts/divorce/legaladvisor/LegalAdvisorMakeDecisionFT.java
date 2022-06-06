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
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class LegalAdvisorMakeDecisionFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision.json";
    private static final String CO_MORE_INFO_RESPONSE = "classpath:responses/response-legal-advisor-make-decision-co-more-info.json";
    private static final String CO_REJECTED_RESPONSE = "classpath:responses/response-legal-advisor-make-decision-co-rejected.json";

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
}
