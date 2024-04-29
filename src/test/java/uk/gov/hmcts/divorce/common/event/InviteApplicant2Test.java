package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.InviteApplicant2.INVITE_APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(SpringExtension.class)
public class InviteApplicant2Test {

    @Mock
    private ApplicationSentForReviewNotification applicationSentForReviewNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private InviteApplicant2 inviteApplicant2;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        inviteApplicant2.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(INVITE_APPLICANT_2);
    }

    @Test
    public void givenEventStartedWithEmptyCaseThenGiveValidationErrors() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = inviteApplicant2.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(8);
        assertThat(response.getErrors().get(0)).isEqualTo("Applicant1FirstName cannot be empty or null");
    }

    @Test
    void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFinancialOrder(null);
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = inviteApplicant2.aboutToSubmit(details, details);

        verifyNoInteractions(notificationDispatcher);

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
        final CaseData caseData = validApplicant1CaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = inviteApplicant2.aboutToSubmit(details, details);

        assertThat(response.getData().getDueDate()).isNotNull();
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenGenerateAccessCodeAndSendEmailToApplicant1AndApplicant2() {
        final CaseData caseData = validApplicant1CaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = inviteApplicant2.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(applicationSentForReviewNotification, caseData, details.getId());

        assertThat(response.getData().getCaseInvite().accessCode()).isNotBlank();
        assertThat(response.getData().getCaseInvite().accessCode().length()).isEqualTo(8);
        assertThat(response.getData().getCaseInvite().accessCode()).doesNotContain("I", "O", "U", "0", "1");
    }

    private Applicant applicant2SolicitorRepresented() {
        return Applicant
            .builder()
            .solicitor(Solicitor
                .builder()
                .email(TEST_SOLICITOR_EMAIL)
                .build()
            )
            .solicitorRepresented(YES)
            .build();
    }
}
