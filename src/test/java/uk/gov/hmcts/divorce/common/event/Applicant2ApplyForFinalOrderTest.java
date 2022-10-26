package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.Applicant2AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.FinalOrderRequestedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.task.ProgressFinalOrderState;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.Applicant2ApplyForFinalOrder.APPLICANT2_FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class Applicant2ApplyForFinalOrderTest {

    @Mock
    private Applicant2AppliedForFinalOrderNotification applicant2AppliedForFinalOrderNotification;

    @Mock
    private FinalOrderRequestedNotification finalOrderRequestedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ProgressFinalOrderState progressFinalOrderState;

    @Mock
    private Clock clock;

    @InjectMocks
    private Applicant2ApplyForFinalOrder applicant2ApplyForFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2ApplyForFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT2_FINAL_ORDER_REQUESTED);
    }

    @Test
    void shouldSendSoleAppliedForFinalOrderNotificationIfSoleApplicationTypeAndAwaitingFinalOrderState() {

        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        caseData.getApplication().setPreviousState(AwaitingFinalOrder);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(applicant2AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendJointAppliedForFinalOrderNotificationToBothSolicitorsIfJointApplicationTypeAndFinalOrderRequestedState() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(FinalOrderRequested);

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(finalOrderRequestedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendBothNotificationsIfCaseProgressedFromAwaitingFinalOrderToFinalOrderRequestedState() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.getApplication().setPreviousState(AwaitingFinalOrder);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(FinalOrderRequested);

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(applicant2AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verify(notificationDispatcher).send(finalOrderRequestedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendSoleAppliedForFinalOrderNotificationFinalOrderOverdueState() {
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(FinalOrderOverdue);

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldUpdateCaseDataAndStateWhenAboutToSubmitIsCalled() {
        LocalDate submittedDate = LocalDate.of(2022, 10, 10);
        setMockClock(clock, submittedDate);

        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(AwaitingFinalOrder);

        final CaseDetails<CaseData, State> updatedCaseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(AwaitingJointFinalOrder);

        when(progressFinalOrderState.apply(caseDetails)).thenReturn(updatedCaseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = applicant2ApplyForFinalOrder.aboutToSubmit(
            caseDetails, caseDetails);

        assertThat(aboutToSubmitResponse.getState()).isEqualTo(AwaitingJointFinalOrder);
        FinalOrder finalOrder = aboutToSubmitResponse.getData().getFinalOrder();
        assertThat(finalOrder.getApplicant1AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.NO);
        assertThat(finalOrder.getApplicant2AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.YES);
        assertThat(finalOrder.getDateFinalOrderSubmitted().toLocalDate()).isEqualTo(submittedDate);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldSetApplicant2SubmittedToYesWhenAboutToSubmitIsTriggered() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.getApplication().setPreviousState(AwaitingFinalOrder);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();

        final CaseDetails<CaseData, State> updatedCaseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(FinalOrderRequested);

        when(progressFinalOrderState.apply(caseDetails)).thenReturn(updatedCaseDetails);

        var response = applicant2ApplyForFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getFinalOrder().getApplicant2SubmittedFinalOrder()).isEqualTo(YesOrNo.YES);
    }
}
