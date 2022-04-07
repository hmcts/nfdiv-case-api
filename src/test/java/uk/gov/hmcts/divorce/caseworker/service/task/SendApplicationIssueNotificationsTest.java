package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.divorce.common.notification.ApplicationIssuedOverseasNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendApplicationIssueNotificationsTest {

    @Mock
    private ApplicationIssuedNotification applicationIssuedNotification;

    @Mock
    private ApplicationIssuedOverseasNotification applicationIssuedOverseasNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendApplicationIssueNotifications underTest;

    @Test
    void shouldSendAllNotifications() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("Spain").build());
        caseData.setCaseInvite(new CaseInvite("applicant2Invite@email.com", null, null));
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingService);

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(applicationIssuedNotification, caseData, caseDetails.getId());
        verify(notificationDispatcher).send(applicationIssuedOverseasNotification, caseData, caseDetails.getId());
    }

    @Test
    void shouldNotSendOverseasNotificationIfNotAwaitingServiceState() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("Spain").build());
        caseData.setCaseInvite(new CaseInvite("", null, null));
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingAos);

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(applicationIssuedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendOverseasNotificationIfNotOverseas() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("UK").build());
        caseData.setCaseInvite(new CaseInvite("", null, null));
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingAos);

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(applicationIssuedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendOverseasNotificationIfJointApplication() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("Spain").build());
        caseData.setCaseInvite(new CaseInvite("", null, null));
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingAos);

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(applicationIssuedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendOverseasNotificationIfPersonalService() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("UK").build());
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseData.setCaseInvite(new CaseInvite("applicant2Invite@email.com", null, null));
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingService);

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(applicationIssuedNotification, caseData, caseDetails.getId());
        verify(notificationDispatcher).send(applicationIssuedOverseasNotification, caseData, caseDetails.getId());
    }
}
