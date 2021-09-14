package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendCitizenSubmissionNotificationsTest {

    @Mock
    private ApplicationOutstandingActionNotification applicationOutstandingActionNotification;

    @Mock
    private ApplicationSubmittedNotification applicationSubmittedNotification;

    @InjectMocks
    private SendCitizenSubmissionNotifications sendCitizenSubmissionNotifications;

    @Test
    void shouldSendCitizenNotificationsIfCitizenApplicationAndSubmittedState() {

        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder().build());
        caseData.setApplicationType(JOINT_APPLICATION);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        sendCitizenSubmissionNotifications.apply(caseDetails);

        verify(applicationSubmittedNotification).sendToApplicant1(caseData, TEST_CASE_ID);
        verify(applicationSubmittedNotification).sendToApplicant2(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendCitizenNotificationIfCitizenApplicationAndAwaitingHwfDecisionState() {

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(Application.builder().build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        sendCitizenSubmissionNotifications.apply(caseDetails);

        verify(applicationSubmittedNotification).sendToApplicant1(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendApplicant1NotificationAndOutstandingNotificationIfCitizenApplicationAndAwaitingDocumentsState() {

        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder().build());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingDocuments);

        sendCitizenSubmissionNotifications.apply(caseDetails);

        verify(applicationSubmittedNotification).sendToApplicant1(caseData, TEST_CASE_ID);
        verify(applicationOutstandingActionNotification).sendToApplicant1(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendApplicant2NotificationAndOutstandingNotificationIfCitizenApplicationAndAwaitingDocumentsState() {

        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder().build());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setApplicant2CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingDocuments);

        sendCitizenSubmissionNotifications.apply(caseDetails);

        verify(applicationSubmittedNotification).sendToApplicant1(caseData, TEST_CASE_ID);
        verify(applicationSubmittedNotification).sendToApplicant2(caseData, TEST_CASE_ID);
        verify(applicationOutstandingActionNotification).sendToApplicant2(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldDoNothingIfCitizenApplicationAndNotSubmittedOrNotAwaitingDocumentState() {

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(Application.builder().build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingPayment);

        sendCitizenSubmissionNotifications.apply(caseDetails);

        verifyNoInteractions(
            applicationSubmittedNotification,
            applicationOutstandingActionNotification);
    }
}
