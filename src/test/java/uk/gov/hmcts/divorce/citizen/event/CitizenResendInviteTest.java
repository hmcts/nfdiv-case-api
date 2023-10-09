package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenResendInvite.CITIZEN_RESEND_INVITE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CitizenResendInviteTest {

    public static final int TO_LINK_TO_CASE_BY_OFFSET_DAYS = 14;

    @Mock
    private ApplicationSentForReviewNotification applicationSentForReviewNotification;

    @Mock
    private Clock clock;

    @InjectMocks
    private CitizenResendInvite citizenResendInvite;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenResendInvite.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_RESEND_INVITE);
    }

    @Test
    public void dataUnchangedWhenApplicant2IsRepresented() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenResendInvite.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    public void dataUnchangedWhenValidationFails() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenResendInvite.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    public void errorReturnedWhenValidationFails() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenResendInvite.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).containsExactly("Not possible to update applicant 2 invite email address");
    }

    @Test
    public void correctFieldsSetUponSuccess() {
        ReflectionTestUtils.setField(citizenResendInvite, "toLinkToCaseOffsetDays", TO_LINK_TO_CASE_BY_OFFSET_DAYS);
        setMockClock(clock);

        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();

        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplication().setApplicant2ReminderSent(YesOrNo.YES);
        caseDetails.setState(State.AwaitingApplicant2Response);
        caseData.setCaseInvite(
            new CaseInvite("app2@email.com", "ACCESS_CODE", "app2_id")
        );

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenResendInvite.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDueDate()).isEqualTo(LocalDate.now(clock).plusDays(TO_LINK_TO_CASE_BY_OFFSET_DAYS));
        assertThat(response.getData().getCaseInvite().accessCode()).isNotEqualTo("ACCESS_CODE");
        assertThat(response.getData().getCaseInvite().accessCode()).isNotBlank();
        assertThat(response.getData().getApplication().getApplicant2ReminderSent()).isNull();
    }

}
