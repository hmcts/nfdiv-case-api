package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreate.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_START_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.ABOUT_TO_SUBMIT_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedCcdCallbackResponse;

@SpringBootTest
public class SolicitorCreateTest extends FunctionalTestSuite {

    @Autowired
    ObjectMapper mapper;

    @Test
    public void shouldUpdateLanguagePreferenceSuccessfullyWhenAboutToStartCallbackIsInvoked()
        throws Exception {
        Map<String, Object> caseDataWithOrganisationPolicy = caseData();
        caseDataWithOrganisationPolicy.put(
            "petitionerOrganisationPolicy",
            OrganisationPolicy
                .<UserRole>builder()
                .orgPolicyCaseAssignedRole(UserRole.PETITIONER_SOLICITOR)
                .build()
        );

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .header(HttpHeaders.AUTHORIZATION, generateIdamTokenForSolicitor())
            .body(
                CallbackRequest
                    .builder()
                    .eventId(SOLICITOR_CREATE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .data(caseDataWithOrganisationPolicy)
                            .build()
                    )
                    .build()
            )
            .when()
            .post(ABOUT_TO_START_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedCcdCallbackResponse("classpath:responses/ccd-callback-set-language-preference.json"),
            response.asString(),
            STRICT
        );
    }

    @Test
    public void shouldUpdateCaseDataWithClaimCostsAndCourtDetailsWhenAboutToSubmitCallbackIsSuccessful()
        throws Exception {
        Map<String, Object> caseData = caseData();
        caseData.put("divorceCostsClaim", YES);

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .header(AUTHORIZATION, generateIdamTokenForSolicitor())
            .body(
                CallbackRequest
                    .builder()
                    .eventId(SOLICITOR_CREATE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .id(1L)
                            .data(caseData)
                            .build()
                    )
                    .build()
            )
            .when()
            .post(ABOUT_TO_SUBMIT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        String expectedResponse = expectedCcdCallbackResponse(
            "classpath:responses/ccd-callback-solicitor-create-about-to-submit.json"
        );
        //replace created date with current date as the handler sets current date
        JsonNode jsonNode = mapper.readTree(expectedResponse);
        JsonNode dataNode = jsonNode.get("data");
        ((ObjectNode) dataNode).put("createdDate", LocalDate.now().toString());

        assertEquals(
            jsonNode.toString(),
            response.asString(),
            STRICT
        );
    }
}
