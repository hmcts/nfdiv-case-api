package uk.gov.hmcts.reform.divorce.caseapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.divorce.caseapi.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.reform.divorce.caseapi.TestResourceUtil.expectedCcdCallbackResponse;
import static uk.gov.hmcts.reform.divorce.caseapi.constants.ControllerConstants.ABOUT_TO_SUBMIT_WEBHOOK;
import static uk.gov.hmcts.reform.divorce.caseapi.constants.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.ccd.event.solicitor.SolicitorCreate.SOLICITOR_CREATE;

@SpringBootTest
public class SolicitorCreateAboutToSubmitTest extends FunctionalTestSuite {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SOLICITOR_CREATE_ABOUT_TO_SUBMIT_CALLBACK_URL = StringUtils.join(
        "/", SOLICITOR_CREATE, ABOUT_TO_SUBMIT_WEBHOOK
    );

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
                    .caseDetails(
                        CaseDetails
                            .builder()
                            .caseId(String.valueOf(createCaseInCcd().getId()))
                            .caseData(caseData)
                            .build()
                    )
                    .build()
            )
            .when()
            .post(SOLICITOR_CREATE_ABOUT_TO_SUBMIT_CALLBACK_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        String expectedResponse = expectedCcdCallbackResponse("classpath:responses/ccd-callback-solicitor-create-about-to-submit.json");
        //replace created date with current date as the handler sets current date
        JsonNode jsonNode = OBJECT_MAPPER.readTree(expectedResponse);
        JsonNode dataNode = jsonNode.get("data");
        ((ObjectNode) dataNode).put("createdDate", LocalDate.now().toString());

        assertEquals(
            jsonNode.toString(),
            response.asString(),
            STRICT
        );
    }
}
