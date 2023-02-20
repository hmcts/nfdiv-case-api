package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAmendApplicationType.CASEWORKER_AMEND_APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerAmendApplicationTypeFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-caseworker-amend-application-type.json";
    private static final String RESPONSE = "classpath:responses/response-caseworker-amend-application-type.json";
    private static final String REQUEST_EMPTY = "classpath:request/casedata/ccd-callback-caseworker-amend-application-type-null-type.json";
    private static final String RESPONSE_EMPTY =  "classpath:responses/response-caseworker-amend-application-type-null-type.json";

    @Test
    public void shouldUpdateCaseDataWithDissolution() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_AMEND_APPLICATION_TYPE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // document_url and document_binary_url are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                RESPONSE
            )));
    }

    @Test
    public void shouldNotUpdateCaseDataDivorceOrDissolution() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_EMPTY);
        final Response response = triggerCallback(caseData, CASEWORKER_AMEND_APPLICATION_TYPE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // document_url and document_binary_url are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                RESPONSE_EMPTY
            )));
    }
}
