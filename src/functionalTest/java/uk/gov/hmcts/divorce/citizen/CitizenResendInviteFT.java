package uk.gov.hmcts.divorce.citizen;

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
import static uk.gov.hmcts.divorce.citizen.event.CitizenResendInvite.CITIZEN_RESEND_INVITE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CitizenResendInviteFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-citizen-resend-invite.json";
    private static final String RESPONSE = "classpath:responses/response-citizen-resend-invite.json";
    private static final String INVALID_REQUEST = "classpath:request/casedata/ccd-callback-casedata-citizen-resend-invite-invalid.json";
    private static final String INVALID_RESPONSE = "classpath:responses/response-citizen-resend-invite-invalid.json";

    @Test
    public void shouldNotSendEmailToApplicant2WhenCaseIsInvalidForResendInvite() throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(CITIZEN_RESEND_INVITE)
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1652969232164603L)
                    .data(caseData(INVALID_REQUEST))
                    .caseTypeId(getCaseType())
                    .state(String.valueOf(AwaitingApplicant2Response))
                    .build()
            )
            .build();

        Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(INVALID_RESPONSE)));
    }

    @Test
    public void shouldSendEmailToApplicant2WhenCaseIsValidForResendInvite() throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(CITIZEN_RESEND_INVITE)
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1652969232164603L)
                    .data(caseData(REQUEST))
                    .caseTypeId(getCaseType())
                    .state(String.valueOf(AwaitingApplicant2Response))
                    .build()
            )
            .build();

        Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));

        assertThatJson(response.asString())
            .inPath("$.data.applicant2ReminderSent")
            .isAbsent();
    }
}
