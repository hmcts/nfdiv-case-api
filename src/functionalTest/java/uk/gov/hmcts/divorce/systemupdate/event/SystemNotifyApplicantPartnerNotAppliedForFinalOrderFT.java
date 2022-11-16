package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantPartnerNotAppliedForFinalOrder.SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class SystemNotifyApplicantPartnerNotAppliedForFinalOrderFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-notify-applicant-partner-not-applied-final-order.json";

    private static final String SOLICITOR_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-notify-solicitor-other-applicant-not-applied-final-order.json";

    @Test
    public void shouldSendEmailToApplicant1WhenFirstInTime() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant1AppliedForFinalOrderFirst", YES);

        Response response = triggerCallback(request, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldWelshSendEmailToApplicant1WhenFirstInTimeAndLanguagePreferenceIsWelsh() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant1AppliedForFinalOrderFirst", YES);
        request.put("applicant1LanguagePreferenceWelsh", YES);

        Response response = triggerCallback(request, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailToApplicant2WhenFirstInTime() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant2AppliedForFinalOrderFirst", YES);

        Response response = triggerCallback(request, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailToApplicant1SolicitorWhenFirstInTime() throws IOException {
        Map<String, Object> request = caseData(SOLICITOR_REQUEST);
        request.put("applicant1AppliedForFinalOrderFirst", YES);

        Response response = triggerCallback(request, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailToApplicantSolicitor2WhenFirstInTime() throws IOException {
        Map<String, Object> request = caseData(SOLICITOR_REQUEST);
        request.put("applicant2AppliedForFinalOrderFirst", YES);

        Response response = triggerCallback(request, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendWelshEmailToApplicant2WhenFirstInTimeAndLanguagePreferenceIsWelsh() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("applicant2AppliedForFinalOrderFirst", YES);
        request.put("applicant2LanguagePreferenceWelsh", YES);

        Response response = triggerCallback(request, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
