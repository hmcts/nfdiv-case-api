package uk.gov.hmcts.divorce.testutil;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.ccd.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.ccd.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreate.SOLICITOR_CREATE;

@TestPropertySource("classpath:application.yaml")
public abstract class FunctionalTestSuite {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);

    @Value("${test-url}")
    protected String testUrl;

    @Value("${idam.s2s-auth.url}")
    protected String s2sUrl;

    @Value("${s2s.name}")
    protected String s2sName;

    @Value("${idam.solicitor.username}")
    protected String solicitorUsername;

    @Value("${idam.solicitor.password}")
    protected String solicitorPassword;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    protected String generateServiceAuthTokenFor(String s2sName) {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.s2sUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(Map.of("microservice", s2sName))
            .when()
            .post("/testing-support/lease")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        return "Bearer " + response.getBody().asString();
    }

    protected String generateIdamTokenForSolicitor() {
        return idamClient.getAccessToken(solicitorUsername, solicitorPassword);
    }


    protected CaseDetails createCaseInCcd() {
        String solicitorToken = generateIdamTokenForSolicitor();
        String s2sTokenForCaseApi = generateServiceAuthTokenFor("nfdiv_case_api");
        String solicitorUserId = idamClient.getUserDetails(solicitorToken).getId();
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
                "languagePreferenceWelsh", "NO",
                "divorceOrDissolution", "divorce",
                "financialOrder", "NO",
                "divorceCostsClaim", "NO",
                "applicant2OrgContactInformation", organisationContactInformation()
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
            CASE_TYPE,
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
            CASE_TYPE,
            true,
            caseDataContent
        );
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url) throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
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
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .header(AUTHORIZATION, generateIdamTokenForSolicitor())
            .body(request)
            .when()
            .post(url);
    }

    public static OrganisationContactInformation organisationContactInformation() {
        return OrganisationContactInformation
            .builder()
            .addressLine1("addressLine1")
            .addressLine2("addressLine2")
            .addressLine3("addressLine3")
            .townCity("townCity")
            .country("country")
            .build();
    }
}
