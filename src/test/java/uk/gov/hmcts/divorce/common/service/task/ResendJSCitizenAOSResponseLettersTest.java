package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ResendJudicialSeparationCitizenAosResponseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ResendJSCitizenAOSResponseLettersTest {

    @Mock
    private ResendJudicialSeparationCitizenAosResponseNotification resendJudicialSeparationCitizenAosResponseNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private ResendJSCitizenAOSResponseLetters resendJSCitizenAOSResponseLetters;

    @Test
    void shouldSendNotifications() {

        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        resendJSCitizenAOSResponseLetters.apply(caseDetails);

        verify(notificationDispatcher).send(resendJudicialSeparationCitizenAosResponseNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
