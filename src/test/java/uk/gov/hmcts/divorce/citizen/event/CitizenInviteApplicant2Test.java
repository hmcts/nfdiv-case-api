package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant1Notification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant2Notification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.citizen.event.CitizenInviteApplicant2.CITIZEN_INVITE_APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseDataMap;

@ExtendWith(SpringExtension.class)
public class CitizenInviteApplicant2Test {

    @Mock
    private ApplicationSentForReviewApplicant1Notification applicationSentForReviewApplicant1Notification;

    @Mock
    private ApplicationSentForReviewApplicant2Notification applicationSentForReviewApplicant2Notification;

    @InjectMocks
    private CitizenInviteApplicant2 citizenInviteApplicant2;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenInviteApplicant2.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_INVITE_APPLICANT_2);
    }

    @Test
    public void givenEventStartedWithEmptyCaseThenGiveValidationErrors() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenInviteApplicant2.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(8);
        assertThat(response.getErrors().get(0)).isEqualTo("Applicant1FirstName cannot be empty or null");
    }

    @Test
    void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        CaseData caseData = caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenInviteApplicant2.aboutToSubmit(details, details);

        verifyNoInteractions(applicationSentForReviewApplicant1Notification);
        verifyNoInteractions(applicationSentForReviewApplicant2Notification);

        assertThat(response.getErrors().size()).isEqualTo(5);
        assertThat(response.getErrors()).containsExactlyInAnyOrder(
            "Applicant1FinancialOrder cannot be empty or null",
            "Applicant2Gender cannot be empty or null",
            "MarriageApplicant1Name cannot be empty or null",
            "MarriageDate cannot be empty or null",
            "JurisdictionConnections cannot be empty or null"
        );
    }

    @Test
    void shouldAddApplicant2DueDateToCaseData() {
        final CaseData caseData = validApplicant1CaseDataMap();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenInviteApplicant2.aboutToSubmit(details, details);

        assertThat(response.getData().getDueDate()).isNotNull();
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenGenerateAccessCodeAndSendEmailToApplicant1AndApplicant2() {
        final CaseData caseData = validApplicant1CaseDataMap();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenInviteApplicant2.aboutToSubmit(details, details);

        verify(applicationSentForReviewApplicant1Notification).send(caseData, details.getId());
        verify(applicationSentForReviewApplicant2Notification).send(caseData, details.getId());

        assertThat(response.getData().getCaseInvite().getAccessCode()).isNotBlank();
        assertThat(response.getData().getCaseInvite().getAccessCode().length()).isEqualTo(8);
        assertThat(response.getData().getCaseInvite().getAccessCode()).doesNotContain("I", "O", "U", "0", "1");
    }
}
