package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.EmailUpdatedNotification;
import uk.gov.hmcts.divorce.common.notification.InviteApplicantToCaseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class EmailUpdateServiceTest {
    @Mock
    InviteApplicantToCaseNotification inviteApplicantToCaseNotification;
    @Mock
    EmailUpdatedNotification emailUpdatedNotification;
    @InjectMocks
    private EmailUpdateService emailUpdateService;

    @Test
    void shouldNotProgressWhenEmailForApplicantIsNull() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setEmail(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        verifyNoInteractions(inviteApplicantToCaseNotification);
        verifyNoInteractions(emailUpdatedNotification);
    }

    @Test
    void shouldNotProgressWhenEmailForApplicantIsBlank() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setEmail("");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        verifyNoInteractions(inviteApplicantToCaseNotification);
        verifyNoInteractions(emailUpdatedNotification);
    }

    @Test
    void shouldNotProgressWhenApplicantIsOffline() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setOffline(YesOrNo.YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        verifyNoInteractions(inviteApplicantToCaseNotification);
        verifyNoInteractions(emailUpdatedNotification);
    }

    @Test
    void shouldNotProgressWhenApplicantIsRepresented() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        verifyNoInteractions(inviteApplicantToCaseNotification);
        verifyNoInteractions(emailUpdatedNotification);
    }

    @Test
    void shouldNotProgressWhenApplicant2AndSoleApplicationNotIssuedYet() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplication().setIssueDate(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, false);

        verifyNoInteractions(inviteApplicantToCaseNotification);
        verifyNoInteractions(emailUpdatedNotification);
    }

    @Test
    void shouldProgressWhenApplicant2AndSoleApplicationIsIssued() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, false);

        verify(inviteApplicantToCaseNotification).send(caseData,TEST_CASE_ID,false);
        verify(emailUpdatedNotification).send(caseData,TEST_CASE_ID,TEST_USER_EMAIL,false);
    }

    @Test
    void shouldProgressWhenApplicant2AndJointApplicationNotIssuedYet() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setIssueDate(null);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, false);

        verify(inviteApplicantToCaseNotification).send(caseData,TEST_CASE_ID,false);
        verify(emailUpdatedNotification).send(caseData,TEST_CASE_ID,TEST_USER_EMAIL,false);
    }

    @Test
    void shouldSetCaseInviteForApplicant1() {
        final CaseData caseData = validApplicant1CaseData();

        caseData.setCaseInviteApp1(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1()).isNotBlank();
        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1().length()).isEqualTo(8);
        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1()).doesNotContain("I", "O", "U", "0", "1");
    }

    @Test
    void shouldSetCaseInviteForApplicant2() {
        final CaseData caseData = validApplicant2CaseData();

        caseData.setCaseInvite(null);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, false);

        assertThat(newDetails.getData().getCaseInvite().accessCode()).isNotBlank();
        assertThat(newDetails.getData().getCaseInvite().accessCode().length()).isEqualTo(8);
        assertThat(newDetails.getData().getCaseInvite().accessCode()).doesNotContain("I", "O", "U", "0", "1");
    }
}
