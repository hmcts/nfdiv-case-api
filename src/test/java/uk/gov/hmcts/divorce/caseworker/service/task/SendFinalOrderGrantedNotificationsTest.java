package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataForGrantFinalOrder;

@ExtendWith(MockitoExtension.class)
class SendFinalOrderGrantedNotificationsTest {

    @Mock
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendFinalOrderGrantedNotifications underTest;

    @Test
    void shouldSendNotifications() {
        CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DivorceOrDissolution.DIVORCE);
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(finalOrderGrantedNotification, caseData, caseDetails.getId());
    }
}
