package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.common.service.ConfirmService.DOCUMENTS_NOT_UPLOADED_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorConfirmService.SOLICITOR_CONFIRM_SERVICE;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorConfirmService.SOLICITOR_SERVICE_AS_THE_SERVICE_METHOD_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfirmServiceCaseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SolicitorConfirmServiceFT extends FunctionalTestSuite {

    private static final String SUBMIT_CONFIRM_SERVICE_JSON = "classpath:responses/response-submit-confirm-service.json";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldSetDueDateTo14DaysAfterServiceDateWhenServiceNotProcessedByProcessServer() throws IOException {
        final var caseData = getConfirmServiceCaseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<>() {});

        Response response = triggerCallback(caseDataMap, SOLICITOR_CONFIRM_SERVICE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(SUBMIT_CONFIRM_SERVICE_JSON)));
    }

    @Test
    public void shouldSetDueDateTo141DaysAfterIssuedDateWhenServiceProcessedByProcessServer() throws IOException {
        final var caseData = getConfirmServiceCaseData();
        caseData.getApplication().getSolicitorService()
            .setServiceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<>() {});

        Response response = triggerCallback(caseDataMap, SOLICITOR_CONFIRM_SERVICE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(SUBMIT_CONFIRM_SERVICE_JSON));
        jsonDocument.set("data.dueDate", caseData.getApplication().getIssueDate().plusDays(141).toString());
        jsonDocument.set("state", State.Holding);

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    public void shouldFailValidationWhenServiceProcessedByProcessServerAndDocumentsNotUploaded() throws IOException {
        final var caseData = getConfirmServiceCaseData();
        caseData.getApplication().getSolicitorService()
            .setServiceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM));
        caseData.getDocuments().setDocumentsUploadedOnConfirmService(null);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<>() {});

        Response response = triggerCallback(caseDataMap, SOLICITOR_CONFIRM_SERVICE, "/callbacks/mid-event?page=SolConfirmService");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString(),
            json -> json.inPath("errors[0]").isEqualTo(DOCUMENTS_NOT_UPLOADED_ERROR)
        );
    }

    @Test
    public void shouldFailValidationWhenServiceMethodIsNotSolicitorService() throws IOException {
        final var caseData = getConfirmServiceCaseData();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<>() {});

        Response response = triggerCallback(caseDataMap, SOLICITOR_CONFIRM_SERVICE, "/callbacks/mid-event?page=SolConfirmService");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString(),
            json -> json.inPath("errors[0]")
                .isEqualTo(SOLICITOR_SERVICE_AS_THE_SERVICE_METHOD_ERROR)
        );
    }

    @Test
    public void shouldNotChangeStateWhenSoleAndAoSSubmitted() throws IOException {
        final var caseData = getConfirmServiceCaseData();
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().dateAosSubmitted(LocalDateTime.now()).build());

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<>() {});

        Response response = triggerCallback(caseDataMap, SOLICITOR_CONFIRM_SERVICE, ABOUT_TO_SUBMIT_URL, State.Holding);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        final String expectedStateJSON = String.format("{state: '%s'}", State.Holding);
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(expectedStateJSON);
    }
}
