package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerReissueApplicationFT extends FunctionalTestSuite {

    private static final String SOLICITOR_REQUEST_DIGITAL_AOS =
        "classpath:request/casedata/ccd-callback-caseworker-reissue-digital-aos-application-about-to-submit.json";
    private static final String SOLICITOR_REQUEST_OFFLINE_AOS =
        "classpath:request/casedata/ccd-callback-caseworker-reissue-offline-aos-application-about-to-submit.json";
    private static final String SOLICITOR_REQUEST_REISSUE_CASE =
        "classpath:request/casedata/ccd-callback-caseworker-reissue-case-type-application-about-to-submit.json";
    private static final String SOLICITOR_RESPONSE =
        "classpath:responses/response-caseworker-reissue-application-about-to-submit.json";
    private static final String SOLICITOR_RESPONSE_REISSUE_TYPE =
        "classpath:responses/response-caseworker-reissue-application-about-to-submit-reissue-case.json";

    @Test
    public void shouldGenerateRespondentAosAndSendEmailToApplicantAndRespondentSolicitorWhenReissueTypeIsDigitalAos() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_DIGITAL_AOS);
        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE
            )));
    }

    @Test
    public void shouldGenerateRespondentAosAndSentAosPackAndNotSendEmailNotificationWhenReissueTypeIsOfflineAos() throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_OFFLINE_AOS);
        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE
            )));
    }

    @Test
    public void shouldGenerateRespondentAosAndMiniApplicationAndSentAosPackAndSendEmailNotificationWhenReissueTypeIsReissueCase()
        throws Exception {
        final Map<String, Object> caseData = caseData(SOLICITOR_REQUEST_REISSUE_CASE);
        final Response response = triggerCallback(caseData, CASEWORKER_REISSUE_APPLICATION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                SOLICITOR_RESPONSE_REISSUE_TYPE
            )));
    }

}
