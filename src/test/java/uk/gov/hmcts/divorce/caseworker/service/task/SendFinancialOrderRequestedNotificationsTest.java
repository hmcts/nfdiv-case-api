package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.FinancialOrderRequestedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendFinancialOrderRequestedNotificationsTest {

    @Mock
    private FinancialOrderRequestedNotification financialOrderRequestedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendFinancialOrderRequestedNotifications underTest;

    @Test
    void shouldNotSendFinancialOrderRequestedNotificationIfFinancialOrderNotRequestedForSole() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingAos);

        underTest.apply(caseDetails);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendFinancialOrderRequestedNotificationIfFinancialOrderNotRequestedForJoint() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingAos);

        underTest.apply(caseDetails);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendFinancialOrderRequestedNotificationIfFinancialOrderRequestedByApplicant1() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setFinancialOrder(YesOrNo.YES);
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingAos);

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(financialOrderRequestedNotification, caseData, caseDetails.getId());
    }

    @Test
    void shouldSendFinancialOrderRequestedNotificationIfFinancialOrderRequestedByApplicant2() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setFinancialOrder(YesOrNo.YES);
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setState(AwaitingAos);

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(financialOrderRequestedNotification, caseData, caseDetails.getId());
    }
}
