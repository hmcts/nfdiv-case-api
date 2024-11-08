package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.EmailUpdatedNotification;
import uk.gov.hmcts.divorce.common.notification.InviteApplicantToCaseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class EmailUpdateServiceTest {
    @Mock
    InviteApplicantToCaseNotification inviteApplicantToCaseNotification;
    @Mock
    EmailUpdatedNotification emailUpdatedNotification;
    @Mock
    NotificationDispatcher notificationDispatcher;
    @InjectMocks
    private EmailUpdateService emailUpdateService;

    @Test
    void shouldSetCaseInviteForApp1AndTriggerNotification() {
        final CaseData caseData = validApplicant1CaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processUpdateForApplicant1(details);

        verify(inviteApplicantToCaseNotification).send(caseData, TEST_CASE_ID, true);
        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1()).isNotBlank();
        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1().length()).isEqualTo(8);
        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1()).doesNotContain("I", "O", "U", "0", "1");
    }

    @Test
    void shouldSetCaseInviteForApp2AndTriggerNotification() {
        final CaseData caseData = validApplicant2CaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processUpdateForApplicant2(details);

        verify(inviteApplicantToCaseNotification).send(caseData, TEST_CASE_ID, false);
        assertThat(newDetails.getData().getCaseInvite().accessCode()).isNotBlank();
        assertThat(newDetails.getData().getCaseInvite().accessCode().length()).isEqualTo(8);
        assertThat(newDetails.getData().getCaseInvite().accessCode()).doesNotContain("I", "O", "U", "0", "1");
    }

    @Test
    void shouldSendNotificationToOldEmail() {
        final CaseData caseData = validApplicant1CaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        emailUpdateService.sendNotificationToOldEmail(details,"test@test.com", true);

        verify(emailUpdatedNotification).send(caseData,TEST_CASE_ID,"test@test.com", true);
    }
}
