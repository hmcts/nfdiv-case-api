package uk.gov.hmcts.reform.divorce.caseapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.caseapi.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestResourceUtil.expectedCcdCallbackResponse;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.ABOUT_TO_START_WEBHOOK;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.ccd.event.solicitor.SolicitorCreate.SOLICITOR_CREATE;

@SpringBootTest
public class DefaultLanguagePreferenceSettingTest extends FunctionalTestSuite {
    private static final String SOLICITOR_CREATE_ABOUT_TO_START_CALLBACK_URL = StringUtils.join(
        "/", SOLICITOR_CREATE, ABOUT_TO_START_WEBHOOK
    );

    @Test
    public void shouldUpdateLanguagePreferenceSuccessfullyWhenAboutToStartCallbackIsInvoked()
        throws Exception {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .header(HttpHeaders.AUTHORIZATION, generateIdamTokenForSolicitor())
            .body(
                CcdCallbackRequest
                    .builder()
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseId(String.valueOf(createCaseInCcd().getId()))
                            .caseData(caseData())
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SOLICITOR_CREATE_ABOUT_TO_START_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedCcdCallbackResponse("classpath:responses/ccd-callback-set-language-preference.json"),
            response.asString(),
            STRICT
        );
    }
}
