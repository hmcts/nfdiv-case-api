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
import uk.gov.hmcts.divorce.solicitor.service.notification.SolicitorSubmittedNotification;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendSubmissionNotificationsTest {

    @Mock
    private SolicitorSubmittedNotification solicitorSubmittedNotification;

    @Mock
    private ApplicationOutstandingActionNotification applicationOutstandingActionNotification;

    @Mock
    private ApplicationSubmittedNotification applicationSubmittedNotification;

    @InjectMocks
    private SendSubmissionNotifications sendSubmissionNotifications;

    @Test
    void shouldSendSolicitorNotificationsIfSolicitorApplication() {

        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder()
            .solSignStatementOfTruth(YES)
            .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        sendSubmissionNotifications.apply(caseDetails);

        verify(solicitorSubmittedNotification).send(caseData, TEST_CASE_ID);
        verifyNoInteractions(applicationSubmittedNotification, applicationOutstandingActionNotification);
    }

    @Test
    void shouldSendCitizenNotificationIfCitizenApplicationAndSubmittedState() {

        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder().build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        sendSubmissionNotifications.apply(caseDetails);

        verify(applicationSubmittedNotification).send(caseData, TEST_CASE_ID);
        verifyNoInteractions(solicitorSubmittedNotification, applicationOutstandingActionNotification);
    }

    @Test
    void shouldSendCitizenNotificationAndOutstandingNotificationIfCitizenApplicationAndAwaitingDocumentsState() {

        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder().build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingDocuments);

        sendSubmissionNotifications.apply(caseDetails);

        verify(applicationSubmittedNotification).send(caseData, TEST_CASE_ID);
        verify(applicationOutstandingActionNotification).send(caseData, TEST_CASE_ID);
        verifyNoInteractions(solicitorSubmittedNotification);
    }
}