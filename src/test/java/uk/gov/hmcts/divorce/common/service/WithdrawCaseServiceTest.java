package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ApplicationWithdrawnNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
public class WithdrawCaseServiceTest {

    @Mock
    private ApplicationWithdrawnNotification applicationWithdrawnNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CcdAccessService caseAccessService;

    @InjectMocks
    private WithdrawCaseService withdrawCaseService;

    @Test
    public void shouldUnlinkApplicantsAndSendNotificationsToApplicant() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = validApplicant2CaseData();
        caseData.setCaseInvite(new CaseInvite(caseData.getCaseInvite().applicant2InviteEmailAddress(), "12345", "12"));
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        withdrawCaseService.withdraw(caseDetails);

        assertThat(caseDetails.getData().getCaseInvite().accessCode()).isNull();
        assertThat(caseDetails.getData().getCaseInvite().applicant2UserId()).isNull();

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_2.getRole()
            )
        ));
        verify(notificationDispatcher).send(applicationWithdrawnNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    public void shouldRemoveSolicitorOrganisationPolicyForRepresentedApplicants() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = validApplicant2CaseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        withdrawCaseService.withdraw(caseDetails);

        assertThat(caseDetails.getData().getApplicant1().getSolicitor().getOrganisationPolicy()).isNull();
        assertThat(caseDetails.getData().getApplicant2().getSolicitor().getOrganisationPolicy()).isNull();

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_2.getRole()
            )
        ));
        verify(notificationDispatcher).send(applicationWithdrawnNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
