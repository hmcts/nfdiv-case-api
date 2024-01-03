package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
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
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerOfflineDocumentVerified.CASEWORKER_OFFLINE_DOCUMENT_VERIFIED;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerOfflineDocumentVerifiedFT extends FunctionalTestSuite {

    private static final String CASEWORKER_D84_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-offline-document-verified-d84-about-to-submit.json";
    private static final String CASEWORKER_D10_JS_UNDISPUTED_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-offline-document-verified-js-undisputed-about-to-submit.json";
    private static final String CASEWORKER_D84_RESPONSE =
        "classpath:responses/response-caseworker-offline-document-verified-d84-about-to-submit.json";
    private static final String CASEWORKER_D10_JS_SOL_DISPUTED_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-offline-document-verified-js-sol-disputed-about-to-submit.json";
    private static final String CASEWORKER_D10_JS_SOL_DISPUTED_SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-offline-document-verified-js-sol-disputed-submitted.json";
    private static final String CASEWORKER_D10_JS_DISPUTED_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-offline-document-verified-js-disputed-about-to-submit.json";
    private static final String CASEWORKER_D10_JS_DISPUTED_SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-offline-document-verified-js-disputed-submitted.json";
    private static final String CASEWORKER_D10_SOL_JS_UNDISPUTED_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-offline-document-verified-js-sol-undisputed-about-to-submit.json";
    private static final String CASEWORKER_D10_SOL_JS_UNDISPUTED_SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-offline-document-verified-js-sol-undisputed-submitted.json";

    @Test
    public void shouldReclassifyScannedDocumentsIfD84Selected() throws IOException {
        final Map<String, Object> caseData = caseData(CASEWORKER_D84_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CASEWORKER_D84_RESPONSE)));
    }

    @Test
    public void shouldSendAosLetterForApp1SolJSDisputed() throws IOException {
        final Map<String, Object> caseData = caseData(CASEWORKER_D10_JS_SOL_DISPUTED_REQUEST);
        caseData.put("coDateD84FormScanned", "2022-01-01T12:12");
        caseData.put("coApplicant1SubmittedDate", "2022-01-18T12:12:12.123");
        caseData.put("dueDate", "2022-01-18");

        final Response response = triggerCallback(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldSendAosLetterForApp1SolJSDisputedForSubmitted() throws IOException {
        final Map<String, Object> caseData = caseData(CASEWORKER_D10_JS_SOL_DISPUTED_SUBMITTED_REQUEST);
        caseData.put("coDateD84FormScanned", "2022-01-01T12:12");
        caseData.put("coApplicant1SubmittedDate", "2022-01-18T12:12:12.123");
        caseData.put("dueDate", "2022-01-18");

        final Response response = triggerCallback(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendAosLetterForJSDisputed() throws IOException {
        final Map<String, Object> caseData = caseData(CASEWORKER_D10_JS_DISPUTED_REQUEST);
        caseData.put("coDateD84FormScanned", "2022-01-01T12:12");
        caseData.put("coApplicant1SubmittedDate", "2022-01-18T12:12:12.123");
        caseData.put("dueDate", "2022-01-18");

        final Response response = triggerCallback(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldSendAosLetterForJSDisputedForSubmitted() throws IOException {
        final Map<String, Object> caseData = caseData(CASEWORKER_D10_JS_DISPUTED_SUBMITTED_REQUEST);
        caseData.put("coDateD84FormScanned", "2022-01-01T12:12");
        caseData.put("coApplicant1SubmittedDate", "2022-01-18T12:12:12.123");
        caseData.put("dueDate", "2022-01-18");

        final Response response = triggerCallback(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendAosLetterForJSUndisputed() throws IOException {
        final Map<String, Object> caseData = caseData(CASEWORKER_D10_JS_UNDISPUTED_REQUEST);
        caseData.put("coDateD84FormScanned", "2022-01-01T12:12");
        caseData.put("coApplicant1SubmittedDate", "2022-01-18T12:12:12.123");
        caseData.put("dueDate", "2022-01-18");

        final Response response = triggerCallback(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendAosLetterForJSSolicitorUndisputed() throws IOException {
        final Map<String, Object> caseData = caseData(CASEWORKER_D10_SOL_JS_UNDISPUTED_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldSendAosLetterForJSSolicitorUndisputedSubmitted() throws IOException {
        final Map<String, Object> caseData = caseData(CASEWORKER_D10_SOL_JS_UNDISPUTED_SUBMITTED_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
