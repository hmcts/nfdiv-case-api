package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.SoleApplicationDisputedNotification;
import uk.gov.hmcts.divorce.common.notification.SoleApplicationNotDisputedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SendAosNotificationsTest {

    @Mock
    private SoleApplicationNotDisputedNotification soleApplicationNotDisputedNotification;

    @Mock
    private SoleApplicationDisputedNotification soleApplicationDisputedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendAosNotifications sendAosNotifications;

    @Test
    void shouldSendDisputedNotifications() {

        final CaseData caseData = CaseData.builder()
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .howToRespondApplication(DISPUTE_DIVORCE)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        sendAosNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(soleApplicationDisputedNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendNotDisputedNotifications() {

        final CaseData caseData = CaseData.builder()
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .howToRespondApplication(WITHOUT_DISPUTE_DIVORCE)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        sendAosNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(soleApplicationNotDisputedNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
