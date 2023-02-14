package uk.gov.hmcts.divorce.common;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SubmitAos.SUBMIT_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SubmitAosFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-solicitor-submit-aos.json";
    private static final String RESPONSE = "classpath:responses/response-solicitor-submit-aos.json";

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessful() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, SUBMIT_AOS, ABOUT_TO_SUBMIT_URL, AosDrafted);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulWhenApplicant1LanguageIsWelsh() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("applicant1LanguagePreferenceWelsh", YES.getValue());

        final Response response = triggerCallback(caseData, SUBMIT_AOS, ABOUT_TO_SUBMIT_URL, AosDrafted);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulWhenApplicant2LanguageIsWelsh() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("applicant2LanguagePreferenceWelsh", YES);
        caseData.put("applicant2SolicitorRepresented", NO);
        caseData.put("acknowledgementOfServiceHowToRespondApplication", WITHOUT_DISPUTE_DIVORCE);

        final Response response = triggerCallback(caseData, SUBMIT_AOS, ABOUT_TO_SUBMIT_URL, AosDrafted);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessfulForOfflineApplicantWithPrivateContact() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);

        caseData.put("applicant1ContactDetailsType", "private");
        caseData.put("applicant1Offline", "Yes");

        final Response response = triggerCallback(caseData, SUBMIT_AOS, ABOUT_TO_SUBMIT_URL, AosDrafted);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
