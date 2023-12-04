package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.AwaitingFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SystemProgressCaseToAwaitingFinalOrderTest {

    @InjectMocks
    private SystemProgressCaseToAwaitingFinalOrder systemProgressCaseToAwaitingFinalOrder;

    @Mock
    private AwaitingFinalOrderNotification awaitingFinalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Test
    void shouldSendNotifications() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        systemProgressCaseToAwaitingFinalOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(awaitingFinalOrderNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
