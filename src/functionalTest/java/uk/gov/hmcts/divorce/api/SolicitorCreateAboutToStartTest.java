package uk.gov.hmcts.divorce.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.api.model.CaseDetails;
import uk.gov.hmcts.divorce.api.model.CcdCallbackRequest;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.api.TestResourceUtil.expectedCcdCallbackResponse;
import static uk.gov.hmcts.divorce.api.constants.ControllerConstants.ABOUT_TO_START_WEBHOOK;
import static uk.gov.hmcts.divorce.api.constants.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.ccd.event.solicitor.SolicitorCreate.SOLICITOR_CREATE;

@SpringBootTest
public class SolicitorCreateAboutToStartTest extends FunctionalTestSuite {
    private static final String SOLICITOR_CREATE_ABOUT_TO_START_CALLBACK_URL = StringUtils.join(
        "/", SOLICITOR_CREATE, ABOUT_TO_START_WEBHOOK
    );

    @Test
    public void shouldUpdateLanguagePreferenceSuccessfullyWhenAboutToStartCallbackIsInvoked()
        throws Exception {
        CaseData caseDataWithOrganisationPolicy = caseData()
            .toBuilder()
            .applicantOrganisationPolicy(
                OrganisationPolicy
                    .<UserRole>builder()
                    .orgPolicyCaseAssignedRole(UserRole.APPLICANT_SOLICITOR)
                    .build()
            )
            .build();

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
                            .caseData(caseDataWithOrganisationPolicy)
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
