package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreateApplication.SOLICITOR_CREATE;

@TestPropertySource("classpath:application.yaml")
public abstract class FunctionalTestSuite {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);

    @Value("${test-url}")
    protected String testUrl;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected IdamService idamService;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;

    @Autowired
    protected CcdSearchService searchService;

    @Autowired
    protected ObjectMapper objectMapper;

    protected CaseDetails createCaseInCcd() {
        String solicitorToken = idamTokenGenerator.generateIdamTokenForSolicitor();
        String s2sTokenForCaseApi = serviceAuthenticationGenerator.generate("nfdiv_case_api");
        String solicitorUserId = idamTokenGenerator.getUserDetailsFor(solicitorToken).getId();
        StartEventResponse startEventResponse = startEventForCreateCase(solicitorToken, s2sTokenForCaseApi, solicitorUserId);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(SOLICITOR_CREATE)
                .summary("Create draft case")
                .description("Create draft case for functional tests")
                .build())
            .data(Map.of(
                "applicant1SolicitorName", "functional test",
                "applicant1LanguagePreferenceWelsh", "NO",
                "divorceOrDissolution", "divorce",
                "applicant1FinancialOrder", "NO"
            ))
            .build();

        return submitNewCase(caseDataContent, solicitorToken, s2sTokenForCaseApi, solicitorUserId);
    }

    private StartEventResponse startEventForCreateCase(
        String solicitorToken,
        String s2sToken,
        String solicitorUserId
    ) {
        // not including in try catch to fail fast the method
        return coreCaseDataApi.startForCaseworker(
            solicitorToken,
            s2sToken,
            solicitorUserId,
            JURISDICTION,
            getCaseType(),
            SOLICITOR_CREATE
        );
    }

    private CaseDetails submitNewCase(
        CaseDataContent caseDataContent,
        String solicitorToken,
        String s2sToken,
        String solicitorUserId
    ) {
        // not including in try catch to fast fail the method
        return coreCaseDataApi.submitForCaseworker(
            solicitorToken,
            s2sToken,
            solicitorUserId,
            JURISDICTION,
            getCaseType(),
            true,
            caseDataContent
        );
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url) throws IOException {

        // Add random Long generation of 16 digits to avoid duplicate case id
        long caseId = ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L, 10_000_000_000_000_000L);

        return triggerCallback(caseData, eventId, url, caseId);
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url, Long caseId) throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                    CaseDetails
                            .builder()
                            .id(caseId)
                            .data(caseData)
                            .createdDate(LOCAL_DATE_TIME)
                            .caseTypeId(getCaseType())
                            .build())
            .caseDetails(
                    CaseDetails
                            .builder()
                            .id(caseId)
                            .data(caseData)
                            .createdDate(LOCAL_DATE_TIME)
                            .caseTypeId(getCaseType())
                            .build()
            )
            .build();

        return triggerCallback(request, url);
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url, State state) throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(getCaseType())
                    .state(state.name())
                    .build()
            )
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(getCaseType())
                    .state(state.name())
                    .build()
            )
            .build();

        return triggerCallback(request, url);
    }

    protected Response triggerCallback(CallbackRequest request, String url) throws IOException {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(request)
            .when()
            .post(url);
    }

    protected List<CaseDetails> searchForCasesWithQuery(BoolQueryBuilder query) {
        return searchService.searchForAllCasesWithQuery(
            query,
            idamService.retrieveSystemUpdateUserDetails(),
            serviceAuthenticationGenerator.generate(),
            Holding
        );
    }

    protected CaseData getCaseData(Map<String, Object> data) {
        return objectMapper.convertValue(data, CaseData.class);
    }
}
