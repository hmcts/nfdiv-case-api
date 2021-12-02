package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkApplicant2.SYSTEM_LINK_APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemLinkApplicant2FT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/system-link-applicant2.json";
    private static final String RESPONSE = "classpath:responses/system-link-applicant2.json";

    @Test
    public void shouldLinkApplicant2WithoutError() throws IOException {
        var app2Token = idamTokenGenerator.generateIdamTokenForSolicitor();
        var app2User = idamService.retrieveUser(app2Token);
        var requestJson = REQUEST.replace("place-holder", app2User.getUserDetails().getId());

        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(SYSTEM_LINK_APPLICANT_2)
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(createCaseInCcd().getId())
                    .data(caseData(requestJson))
                    .caseTypeId(CASE_TYPE)
                    .build()
            )
            .build();

        Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL, idamTokenGenerator.generateIdamTokenForSystem());

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

}
