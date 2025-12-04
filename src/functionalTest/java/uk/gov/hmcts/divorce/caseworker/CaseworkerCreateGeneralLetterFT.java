package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralLetter.CASEWORKER_CREATE_GENERAL_LETTER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCreateGeneralLetterFT extends FunctionalTestSuite {

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldSetGeneralOrdersAndUpdateCaseDataWhenAboutToSubmitCallbacksIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData("classpath:request/casedata/ccd-callback-general-letter-about-to-submit.json");

        final Response aboutToSubmitResponse = triggerCallback(
            caseData,
            CASEWORKER_CREATE_GENERAL_LETTER,
            ABOUT_TO_SUBMIT_URL
        );

        assertThat(aboutToSubmitResponse.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(aboutToSubmitResponse.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-general-letter-about-to-submit.json"
            )));
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldSendNotificationsWhenSubmittedCallbackIsSuccessfulForSoleCitizenApplication() throws Exception {
        final Map<String, Object> caseData = caseData("classpath:request/casedata/ccd-callback-general-letter-submitted.json");

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_GENERAL_LETTER, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
