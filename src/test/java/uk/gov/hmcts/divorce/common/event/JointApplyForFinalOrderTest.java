package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.notification.Applicant2AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.task.ProgressFinalOrderState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.JointApplyForFinalOrder.JOINT_FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class JointApplyForFinalOrderTest {

    @Mock
    private Applicant2AppliedForFinalOrderNotification applicant2AppliedForFinalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ProgressFinalOrderState progressFinalOrderState;

    @InjectMocks
    private JointApplyForFinalOrder jointApplyForFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        jointApplyForFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(JOINT_FINAL_ORDER_REQUESTED);
    }

    @Test
    void shouldSendSoleAppliedForFinalOrderNotificationIfSoleApplicationTypeAndAwaitingFinalOrderState() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(AwaitingFinalOrder);

        when(progressFinalOrderState.apply(caseDetails)).thenReturn(caseDetails);
        jointApplyForFinalOrder.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(applicant2AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendSoleAppliedForFinalOrderNotificationFinalOrderOverdueState() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();
        caseDetails.setState(FinalOrderOverdue);

        when(progressFinalOrderState.apply(caseDetails)).thenReturn(caseDetails);
        jointApplyForFinalOrder.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher, never()).send(applicant2AppliedForFinalOrderNotification, caseData, caseDetails.getId());
    }
}
