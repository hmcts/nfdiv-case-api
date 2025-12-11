package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralLetter.CASEWORKER_CREATE_GENERAL_LETTER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class CaseworkerCreateGeneralLetterFT extends FunctionalTestSuite {

    @Test
    public void shouldSetGeneralOrdersAndUpdateCaseDataWhenAboutToSubmitCallbacksIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData("classpath:request/casedata/ccd-callback-general-letter-about-to-submit.json");

        final Response aboutToSubmitResponse = triggerCallback(
            caseData,
            CASEWORKER_CREATE_GENERAL_LETTER,
            ABOUT_TO_SUBMIT_URL
        );

        assertThat(aboutToSubmitResponse.getStatusCode()).isEqualTo(OK.value());
    }
}
