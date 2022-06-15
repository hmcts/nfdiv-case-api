package uk.gov.hmcts.divorce.common;

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
import static uk.gov.hmcts.divorce.common.event.InviteApplicant2.INVITE_APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class InviteApplicant2FT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-applicant1-invite-applicant2.json";
    private static final String RESPONSE =
        "classpath:responses/response-applicant1-invite-applicant2.json";

    private static final String SOLICITOR_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-applicant1-invite-applicant2-solicitor.json";
    private static final String SOLICITOR_RESPONSE =
        "classpath:responses/response-applicant1-invite-applicant2-solicitor.json";


    @Test
    public void shouldSendEmailToApplicant1AndApplicant2WhenAllTemplateParamsAreValid() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, INVITE_APPLICANT_2, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldSendEmailToApplicant1AndApplicant2WhenAllTemplateParamsAreValidAndApplicant1LanguageWelsh()
        throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant1LanguagePreferenceWelsh", YES);

        Response response = triggerCallback(request, INVITE_APPLICANT_2, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailToApplicant2WhenAllTemplateParamsAreValidAndApplicant2IsRepresented() throws IOException {
        Map<String, Object> request = caseData(SOLICITOR_REQUEST);

        Response response = triggerCallback(request, INVITE_APPLICANT_2, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(SOLICITOR_RESPONSE)));
    }
}
