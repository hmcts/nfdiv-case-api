package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendApplicationIssueNotificationsTest {

    @Mock
    private ApplicationIssuedNotification notification;

    @InjectMocks
    private SendApplicationIssueNotifications underTest;

    @Test
    void shouldSendSoleNotifications() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setHomeAddress(AddressGlobalUK.builder().country("Spain").build());
        caseData.getCaseInvite().setApplicant2InviteEmailAddress("applicant2Invite@email.com");
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingAos);
        caseDetails.setState(AwaitingAos);

        underTest.apply(caseDetails);

        verify(notification).sendToSoleApplicant1(eq(caseData), eq(caseDetails.getId()));
        verify(notification).sendToSoleRespondent(eq(caseData), eq(caseDetails.getId()));
        verify(notification).notifyApplicantOfServiceToOverseasRespondent(eq(caseData), eq(caseDetails.getId()));
    }

    @Test
    void shouldSendJointNotifications() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(Application.builder().applicant1KnowsApplicant2EmailAddress(YES).build());
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.apply(caseDetails);

        verify(notification).sendToJointApplicant1(eq(caseData), eq(caseDetails.getId()));
        verify(notification).sendToJointApplicant2(eq(caseData), eq(caseDetails.getId()));
    }
}
