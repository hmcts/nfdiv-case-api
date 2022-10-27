package uk.gov.hmcts.divorce.citizen;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2NotBroken.APPLICANT_2_NOT_BROKEN;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CitizenApplicant2NotBrokenFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-applicant2-not-broken.json";
    private static final String RESPONSE = "classpath:responses/response-applicant2-not-broken.json";

    @Test
    public void shouldSendEmailToApplicant1AndApplicant2WhenAllTemplateParamsAreValid() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, APPLICANT_2_NOT_BROKEN, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldSendWelshEmailsToApplicant1AndApplicant2WhenAllTemplateParamsAreValidAndLanguagePreferenceIsWelsh()
        throws IOException {

        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant1LanguagePreferenceWelsh", YES);
        request.put("applicant2LanguagePreferenceWelsh", YES);

        Response response = triggerCallback(request, APPLICANT_2_NOT_BROKEN, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
