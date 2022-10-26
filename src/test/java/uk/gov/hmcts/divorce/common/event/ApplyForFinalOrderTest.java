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
import uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification;
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
import static uk.gov.hmcts.divorce.common.event.ApplyForFinalOrder.FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class ApplyForFinalOrderTest {

    @Mock
    private Applicant1AppliedForFinalOrderNotification applicant1AppliedForFinalOrderNotification;

    @Mock
    private FinalOrderRequestedNotification finalOrderRequestedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ProgressFinalOrderState progressFinalOrderState;

    @Mock
    private Clock clock;

    @InjectMocks
    private ApplyForFinalOrder applyForFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applyForFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(FINAL_ORDER_REQUESTED);
    }

    @Test
    void shouldSendSoleAppliedForFinalOrderNotificationIfSoleApplicationTypeAndAwaitingFinalOrderState() {
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        caseData.getApplication().setPreviousState(AwaitingFinalOrder);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();

        applyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(applicant1AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendJointAppliedForFinalOrderNotificationToBothSolicitorsIfJointApplicationTypeAndFinalOrderRequestedState() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(FinalOrderRequested);

        applyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(finalOrderRequestedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendBothNotificationsIfCaseProgressedFromAwaitingFinalOrderToFinalOrderRequestedState() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.getApplication().setPreviousState(AwaitingFinalOrder);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(FinalOrderRequested);

        applyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(applicant1AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verify(notificationDispatcher).send(finalOrderRequestedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendSoleAppliedForFinalOrderNotificationFinalOrderOverdueState() {
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(FinalOrderOverdue);

        applyForFinalOrder.submitted(caseDetails, null);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldUpdateCaseDataAndStateWhenAboutToSubmitIsCalled() {
        LocalDate submittedDate = LocalDate.of(2022, 10, 10);
        setMockClock(clock, submittedDate);

        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(AwaitingFinalOrder);

        final CaseDetails<CaseData, State> updatedCaseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(FinalOrderRequested);

        when(progressFinalOrderState.apply(caseDetails)).thenReturn(updatedCaseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = applyForFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(aboutToSubmitResponse.getState()).isEqualTo(FinalOrderRequested);
        FinalOrder finalOrder = aboutToSubmitResponse.getData().getFinalOrder();
        assertThat(finalOrder.getApplicant1AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.YES);
        assertThat(finalOrder.getApplicant2AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.NO);
        assertThat(finalOrder.getDateFinalOrderSubmitted().toLocalDate()).isEqualTo(submittedDate);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldSetApplicant1SubmittedToYesWhenAboutToSubmitIsTriggered() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.getApplication().setPreviousState(AwaitingFinalOrder);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();

        final CaseDetails<CaseData, State> updatedCaseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(FinalOrderRequested);

        when(progressFinalOrderState.apply(caseDetails)).thenReturn(updatedCaseDetails);

        var response = applyForFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getFinalOrder().getApplicant1SubmittedFinalOrder()).isEqualTo(YesOrNo.YES);
    }
}
