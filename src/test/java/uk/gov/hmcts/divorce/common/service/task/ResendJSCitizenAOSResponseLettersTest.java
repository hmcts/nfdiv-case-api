package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ResendJudicialSeparationCitizenAosResponseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.service.task.ResendJSCitizenAOSResponseLetters.NOTIFICATION_TEMPLATE_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ResendJSCitizenAOSResponseLettersTest {

    @Mock
    private ResendJudicialSeparationCitizenAosResponseNotification resendJudicialSeparationCitizenAosResponseNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private Logger logger;

    @InjectMocks
    private ResendJSCitizenAOSResponseLetters resendJSCitizenAOSResponseLetters;

    @Test
    void shouldSendNotifications() {

        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        CaseDetails<CaseData, State> response = resendJSCitizenAOSResponseLetters.apply(caseDetails);

        verify(notificationDispatcher).send(resendJudicialSeparationCitizenAosResponseNotification, caseData, TEST_CASE_ID);
        assertThat(response.getData().getApplication().getJsCitizenAosResponseLettersResent()).isEqualTo(YES);
    }

    @Test
    void shouldLogErrorIfNotificationFails() {
        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final NotificationTemplateException notificationTemplateException = new NotificationTemplateException("error");

        doThrow(notificationTemplateException).when(notificationDispatcher).send(any(), any(), any());

        CaseDetails<CaseData, State> response = resendJSCitizenAOSResponseLetters.apply(caseDetails);

        verify(logger).error(eq(NOTIFICATION_TEMPLATE_ERROR), eq("error"), eq(notificationTemplateException));
        assertThat(response.getData().getApplication().getJsCitizenAosResponseLettersResent()).isEqualTo(NO);
    }
}
