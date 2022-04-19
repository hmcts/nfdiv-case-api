package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.SoleApplicationSolicitorSubmitAosNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class SendSolicitorAosNotificationsTest {

    @Mock
    private SoleApplicationSolicitorSubmitAosNotification soleApplicationSolicitorSubmitAosNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendSolicitorAosNotifications sendSolicitorAosNotifications;

    @Test
    void shouldSendAosSubmittedNotificationsIfApplicant2Represented() {
        final CaseData caseData = CaseData.builder()
            .applicant2(respondentWithDigitalSolicitor())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(caseData);

        sendSolicitorAosNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(soleApplicationSolicitorSubmitAosNotification, caseData, 1L);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendAosSubmittedNotificationsIfApplicant2NotRepresented() {
        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(caseData);

        sendSolicitorAosNotifications.apply(caseDetails);

        verifyNoInteractions(notificationDispatcher);
    }
}
