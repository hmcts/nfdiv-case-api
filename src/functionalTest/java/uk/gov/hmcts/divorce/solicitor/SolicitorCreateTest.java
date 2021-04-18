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
import uk.gov.hmcts.divorce.FunctionalTestSuite;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.UserRole;
import uk.gov.hmcts.divorce.model.CaseDetails;
import uk.gov.hmcts.divorce.model.CcdCallbackRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.TestResourceUtil.ABOUT_TO_START_CALLBACK_URL;
import static uk.gov.hmcts.divorce.TestResourceUtil.ABOUT_TO_SUBMIT_CALLBACK_URL;
import static uk.gov.hmcts.divorce.TestResourceUtil.expectedCcdCallbackResponse;
import static uk.gov.hmcts.divorce.constants.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreate.SOLICITOR_CREATE;

@SpringBootTest
public class SolicitorCreateTest extends FunctionalTestSuite {

    @Autowired
    ObjectMapper mapper;

    @Test
    public void shouldUpdateLanguagePreferenceSuccessfullyWhenAboutToStartCallbackIsInvoked()
        throws Exception {
        CaseData caseDataWithOrganisationPolicy = caseData()
            .toBuilder()
            .petitionerOrganisationPolicy(
                OrganisationPolicy
                    .<UserRole>builder()
                    .orgPolicyCaseAssignedRole(UserRole.PETITIONER_SOLICITOR)
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
                    .eventId(SOLICITOR_CREATE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseData(caseDataWithOrganisationPolicy)
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
        CaseData caseData = caseData();
        caseData.setDivorceCostsClaim(YES);

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, generateServiceAuthTokenFor(s2sName))
            .header(AUTHORIZATION, generateIdamTokenForSolicitor())
            .body(
                CcdCallbackRequest
                    .builder()
                    .eventId(SOLICITOR_CREATE)
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseId(1L)
                            .caseData(caseData)
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
