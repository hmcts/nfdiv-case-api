package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejected.CASEWORKER_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;


@SpringBootTest
public class CaseworkerRejectedFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-caseworker-reject-application-about-to-submit.json";

    @Disabled
    @Test
    public void shouldSetPreviousStateWhenAboutToSubmitCallbackIsSuccessful() throws Exception {

        // This caseId will need to be updated if the cases in AAT are cleared
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(CASEWORKER_REJECTED)
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1652969232164603L)
                    .data(caseData(REQUEST))
                    .caseTypeId(getCaseType())
                    .state("Submitted")
                    .build()
            )
            .build();

        final Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString(),
            json -> json.inPath("data.previousState").isEqualTo(Submitted.name())
        );
    }
}
