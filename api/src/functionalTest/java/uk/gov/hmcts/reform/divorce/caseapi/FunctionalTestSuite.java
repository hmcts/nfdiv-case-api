package uk.gov.hmcts.reform.divorce.caseapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SAVE_AND_CLOSE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SUBMITTED_WEBHOOK;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution.DIVORCE;

@TestPropertySource("classpath:application.yaml")
abstract class FunctionalTestSuite {
    protected static final String TEST_USER_EMAIL = "simulate-delivered@notifications.service.gov.uk";
    protected static final String TEST_FIRST_NAME = "John";
    protected static final String TEST_LAST_NAME = "Smith";

    protected static final String SAVE_AND_SIGN_OUT_CALLBACK_URL = StringUtils.join(
        "/", SAVE_AND_CLOSE, SUBMITTED_WEBHOOK
    );

    @Value("${test-url}")
    protected String testUrl;

    @Value("${idam.s2s-auth.url}")
    protected String s2sUrl;

    @Value("${s2s.name}")
    protected String s2sName;

    @Value("${idam.solicitor.username}")
    protected String solicitorUsername;

    @Value("${idam.solicitor.password")
    protected String solicitorPassword;

    @Autowired
    private IdamClient idamClient;

    protected CaseData caseData() {
        CaseData caseData = new CaseData();
        caseData.setD8PetitionerFirstName(TEST_FIRST_NAME);
        caseData.setD8PetitionerLastName(TEST_LAST_NAME);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setD8PetitionerEmail(TEST_USER_EMAIL);
        return caseData;
    }

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

        return "Bearer " + response.getBody().print();
    }

    protected String generateIdamTokenForSolicitor() {
        return idamClient.getAccessToken(solicitorUsername,solicitorPassword);
    }
}
