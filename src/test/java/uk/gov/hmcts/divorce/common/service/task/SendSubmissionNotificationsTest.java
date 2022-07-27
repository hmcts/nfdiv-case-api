package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendSubmissionNotificationsTest {

    @Mock
    private ApplicationOutstandingActionNotification applicationOutstandingActionNotification;

    @Mock
    private ApplicationSubmittedNotification applicationSubmittedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendSubmissionNotifications sendSubmissionNotifications;

    @Test
    void shouldDispatchSubmittedNotificationsAndOutstandingActionNotificationsIfSubmittedState() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        sendSubmissionNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(applicationSubmittedNotification, caseData, TEST_CASE_ID);
        verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldDispatchOutstandingAndSubmittedNotificationIfAwaitingHwfDecisionState() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        sendSubmissionNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(applicationSubmittedNotification, caseData, TEST_CASE_ID);
        verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldDispatchSubmittedNotificationsAndOutstandingActionNotificationsIfWelshPreviousStateSubmitted() {
        final CaseData caseData = caseData();
        caseData.getApplication().setWelshPreviousState(Submitted);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(WelshTranslationReview);

        sendSubmissionNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(applicationSubmittedNotification, caseData, TEST_CASE_ID);
        verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldDispatchSubmittedNotificationsAndOutstandingActionNotificationsIfWelshPreviousStateAwaitingHwfDecision() {
        final CaseData caseData = caseData();
        caseData.getApplication().setWelshPreviousState(AwaitingHWFDecision);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(WelshTranslationReview);

        sendSubmissionNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(applicationSubmittedNotification, caseData, TEST_CASE_ID);
        verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldOnlyDispatchOutstandingNotificationIfAwaitingHwfDecisionStateAndCannotUpload() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(DocumentType.MARRIAGE_CERTIFICATE));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        sendSubmissionNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldOnlyDispatchOutstandingNotificationIfAwaitingDocumentsState() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingDocuments);

        sendSubmissionNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotDispatchSubmittedNotificationsIfOtherState() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingPayment);

        sendSubmissionNotifications.apply(caseDetails);

        verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
