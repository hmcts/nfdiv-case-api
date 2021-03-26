package uk.gov.hmcts.reform.divorce.caseapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.divorce.caseapi.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.ABOUT_TO_START_WEBHOOK;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SUBMIT_PETITION;

@SpringBootTest
public class SolicitorSubmitPetitionTest extends FunctionalTestSuite {

    private static final String SUBMIT_PETITION_ABOUT_TO_START_CALLBACK_URL = StringUtils.join(
        "/", SUBMIT_PETITION, ABOUT_TO_START_WEBHOOK
    );

    @Test
    public void shouldUpdateCaseDataWithOrderSummaryWhenIssueFeeIsSuccessfullyRetrieved() throws Exception {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .header(HttpHeaders.AUTHORIZATION,generateIdamTokenForSolicitor())
            .body(
                CcdCallbackRequest
                    .builder()
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseData())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SUBMIT_PETITION_ABOUT_TO_START_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        JSONAssert.assertEquals(
            expectedCcdCallbackResponse(),
            response.asString(),
            false
        );
    }

    private String expectedCcdCallbackResponse() throws IOException {
        File issueFeesResponseJsonFile = ResourceUtils.getFile("classpath:responses/ccd-callback-submit-petition.json");
        return new String(Files.readAllBytes(issueFeesResponseJsonFile.toPath()));
    }
}
