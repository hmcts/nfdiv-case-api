package uk.gov.hmcts.divorce.legaladvisor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeServiceDecision.LEGAL_ADVISOR_SERVICE_DECISION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class LegalAdvisorMakeServiceDecisionFT extends FunctionalTestSuite {

    @Test
    public void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateOrderToDispenseDocIfApplicationIsGrantedAndTypeIsDispensed()
        throws Exception {
        final Map<String, Object> caseData = caseData("classpath:request/casedata/ccd-callback-make-service-decision.json");
        caseData.put("alternativeServiceType", "dispensed");
        caseData.put("applicationType", "soleApplication");

        final Response aboutToSubmitResponse = triggerCallback(caseData, LEGAL_ADVISOR_SERVICE_DECISION, ABOUT_TO_SUBMIT_URL);
        assertThat(aboutToSubmitResponse.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(aboutToSubmitResponse.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-la-make-service-decision-dispensed.json"
            )));
    }

    @Test
    public void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateDeemedServiceOrderDocIfApplicationIsGrantedAndTypeIsDeemed()
        throws Exception {
        final Map<String, Object> caseData = caseData("classpath:request/casedata/ccd-callback-make-service-decision.json");
        caseData.put("alternativeServiceType", "deemed");
        caseData.put("deemedServiceDate","2021-06-20");
        caseData.put("applicationType", "jointApplication");

        final Response aboutToSubmitResponse = triggerCallback(caseData, LEGAL_ADVISOR_SERVICE_DECISION, ABOUT_TO_SUBMIT_URL);
        assertThat(aboutToSubmitResponse.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(aboutToSubmitResponse.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-la-make-service-decision-deemed.json"
            )));
    }

    @Test
    public void shouldUpdateStateToAwaitingAoSAndGenerateOrderToDispenseRefusedDocIfApplicationIsNotGrantedAndTypeIsDispensed()
        throws Exception {
        final Map<String, Object> caseData = caseData(
            "classpath:request/casedata/ccd-callback-make-service-decision-not-granted.json");
        caseData.put("alternativeServiceType", "dispensed");

        final Response aboutToSubmitResponse = triggerCallback(caseData, LEGAL_ADVISOR_SERVICE_DECISION, ABOUT_TO_SUBMIT_URL);
        assertThat(aboutToSubmitResponse.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(aboutToSubmitResponse.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-la-make-service-decision-dispensed-with-refused.json"
            )));
    }

    @Test
    public void shouldUpdateStateToAwaitingAoSAndGenerateDeemedRefusedDocIfApplicationIsNotGrantedAndTypeIsDeemed()
        throws Exception {
        final Map<String, Object> caseData = caseData(
            "classpath:request/casedata/ccd-callback-make-service-decision-not-granted.json");
        caseData.put("alternativeServiceType", "deemed");

        final Response aboutToSubmitResponse = triggerCallback(caseData, LEGAL_ADVISOR_SERVICE_DECISION, ABOUT_TO_SUBMIT_URL);
        assertThat(aboutToSubmitResponse.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(aboutToSubmitResponse.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-la-make-service-decision-deemed-refused.json"
            )));
    }
}
