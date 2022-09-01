package uk.gov.hmcts.divorce.common;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
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
    private static final String DOCUMENTS_UPLOADED_REQUEST_JOINT =
            "classpath:request/casedata/ccd-callback-casedata-submit-clarification-documents-uploaded-joint.json";
    private static final String CANNOT_UPLOAD_DOCUMENTS_RESPONSE =
        "classpath:responses/response-submit-clarification-cannot-upload.json";
    private static final String DOCUMENTS_UPLOADED_RESPONSE =
        "classpath:responses/response-submit-clarification-documents-uploaded.json";
    private static final String DOCUMENTS_UPLOADED_RESPONSE_JOINT =
            "classpath:responses/response-submit-clarification-documents-uploaded-joint.json";

    //TODO: This id will need to be updated with the new case id for joint application if AAT database is refreshed
    private static final Long CASE_ID_JOINT = 1652180348849042L;

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

    @Test
    public void shouldAddClarificationDocumentsToDocumentsUploadedWhenLangPreferredIsWelsh() throws IOException {
        Map<String, Object> request = caseData(DOCUMENTS_UPLOADED_REQUEST);
        request.put("applicant1LanguagePreferenceWelsh", "Yes");
        request.put("applicant2LanguagePreferenceWelsh", "Yes");

        Response response = triggerCallback(request, SUBMIT_CLARIFICATION, ABOUT_TO_SUBMIT_URL);

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(DOCUMENTS_UPLOADED_RESPONSE));
        jsonDocument.set("data.applicant1LanguagePreferenceWelsh", "Yes");
        jsonDocument.set("data.applicant2LanguagePreferenceWelsh", "Yes");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    public void shouldAddClarificationDocumentsToDocumentsUploadedForJointApplication() throws IOException {
        Map<String, Object> request = caseData(DOCUMENTS_UPLOADED_REQUEST_JOINT);

        Response response = triggerCallback(request, SUBMIT_CLARIFICATION, ABOUT_TO_SUBMIT_URL, CASE_ID_JOINT);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
                .when(IGNORING_EXTRA_FIELDS)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(json(expectedResponse(DOCUMENTS_UPLOADED_RESPONSE_JOINT)));
    }

    @Test
    public void shouldAddClarificationDocumentsToDocumentsUploadedWhenLangPreferredIsWelshAndJointApplication() throws IOException {
        Map<String, Object> request = caseData(DOCUMENTS_UPLOADED_REQUEST_JOINT);
        request.put("applicant1LanguagePreferenceWelsh", "Yes");
        request.put("applicant2LanguagePreferenceWelsh", "Yes");

        Response response = triggerCallback(request, SUBMIT_CLARIFICATION, ABOUT_TO_SUBMIT_URL, CASE_ID_JOINT);

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(DOCUMENTS_UPLOADED_RESPONSE_JOINT));
        jsonDocument.set("data.applicant1LanguagePreferenceWelsh", "Yes");
        jsonDocument.set("data.applicant2LanguagePreferenceWelsh", "Yes");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
                .when(IGNORING_EXTRA_FIELDS)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(jsonDocument.json());
    }
}
