package uk.gov.hmcts.divorce.common;

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
import static uk.gov.hmcts.divorce.common.event.Applicant2Approve.APPLICANT_2_APPROVE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class Applicant2ApproveFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-applicant2-approved.json";
    private static final String RESPONSE = "classpath:responses/response-applicant2-approved.json";

    private static final String SOLICITOR_REQUEST = "classpath:request/casedata/ccd-callback-casedata-solicitor-applicant2-approved.json";
    private static final String SOLICITOR_RESPONSE = "classpath:responses/solicitor-response-applicant2-approved.json";

    @Test
    public void shouldSendEmailToApplicant1AndApplicant2WhenAllTemplateParamsAreValid() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, APPLICANT_2_APPROVE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldSendEmailToApplicant1SolicitorAndApplicant2WhenAllTemplateParamsAreValid() throws IOException {
        Map<String, Object> request = caseData(SOLICITOR_REQUEST);

        Response response = triggerCallback(request, APPLICANT_2_APPROVE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(SOLICITOR_RESPONSE)));
    }
}
