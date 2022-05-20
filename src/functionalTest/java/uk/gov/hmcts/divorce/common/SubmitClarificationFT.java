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
import static uk.gov.hmcts.divorce.common.event.SubmitClarification.SUBMIT_CLARIFICATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SubmitClarificationFT extends FunctionalTestSuite {

    private static final String CANNOT_UPLOAD_DOCUMENTS_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-submit-clarification-cannot-upload.json";
    private static final String DOCUMENTS_UPLOADED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-submit-clarification-documents-uploaded.json";
    private static final String CANNOT_UPLOAD_DOCUMENTS_RESPONSE =
        "classpath:responses/response-submit-clarification-cannot-upload.json";
    private static final String DOCUMENTS_UPLOADED_RESPONSE =
        "classpath:responses/response-submit-clarification-documents-uploaded.json";

    @Test
    public void shouldSendEmailAndReturn200WhenCaseDataIsAppropriate() throws IOException {
        Map<String, Object> request = caseData(CANNOT_UPLOAD_DOCUMENTS_REQUEST);

        Response response = triggerCallback(request, SUBMIT_CLARIFICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CANNOT_UPLOAD_DOCUMENTS_RESPONSE)));
    }

    @Test
    public void shouldAddClarificationDocumentsToDocumentsUploaded() throws IOException {
        Map<String, Object> request = caseData(DOCUMENTS_UPLOADED_REQUEST);

        Response response = triggerCallback(request, SUBMIT_CLARIFICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(DOCUMENTS_UPLOADED_RESPONSE)));
    }
}
