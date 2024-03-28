package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.ResendConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SendRegeneratedCOPronouncedCoverLettersTest {

    @Mock
    private ResendConditionalOrderPronouncedNotification resendCoverLetterNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendRegeneratedCOPronouncedCoverLetters underTest;

    @Test
    public void shouldSendRegeneratedLetters() {
        final CaseData caseData = caseData();
        Applicant applicant2 = Applicant.builder()
            .coPronouncedCoverLetterRegenerated(YES)
            .offline(YesOrNo.YES)
            .solicitorRepresented(YesOrNo.NO)
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseData.setApplicant2(applicant2);
        caseDetails.setData(caseData);
        underTest.apply(caseDetails);

        assertThat(caseDetails.getData().getApplication().getCoPronouncedForceConfidentialCoverLetterResentAgain()).isEqualTo(YES);

        verify(notificationDispatcher).send(resendCoverLetterNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    public void shouldSetTheFlagCoPronouncedCoverLetterResentToNoWhenNotificationDispatcherThrowsException() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        doThrow(new NotificationTemplateException("some error"))
            .when(notificationDispatcher).send(resendCoverLetterNotification, caseData, TEST_CASE_ID);

        underTest.apply(caseDetails);

        assertThat(caseDetails.getData().getApplication().getCoPronouncedForceConfidentialCoverLetterResentAgain()).isEqualTo(NO);
    }
}
