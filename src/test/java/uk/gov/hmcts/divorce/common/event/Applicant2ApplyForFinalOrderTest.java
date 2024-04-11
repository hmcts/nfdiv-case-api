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
import uk.gov.hmcts.divorce.common.notification.Applicant2AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.FinalOrderRequestedNotification;
import uk.gov.hmcts.divorce.common.service.ApplyForFinalOrderService;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.Applicant2ApplyForFinalOrder.APPLICANT2_FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class Applicant2ApplyForFinalOrderTest {

    @Mock
    private Applicant2AppliedForFinalOrderNotification applicant2AppliedForFinalOrderNotification;

    @Mock
    private FinalOrderRequestedNotification finalOrderRequestedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ApplyForFinalOrderService applyForFinalOrderService;

    @Mock
    private GeneralReferralService generalReferralService;

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
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(applicant2AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendJointAppliedForFinalOrderNotificationToBothSolicitorsIfJointApplicationTypeAndFinalOrderRequestedState() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();
        caseDetails.setState(FinalOrderRequested);

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(finalOrderRequestedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendBothNotificationsIfCaseProgressedFromAwaitingFinalOrderToFinalOrderRequestedState() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.getApplication().setPreviousState(AwaitingFinalOrder);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();
        caseDetails.setState(FinalOrderRequested);

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(applicant2AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verify(notificationDispatcher).send(finalOrderRequestedNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendSoleAppliedForFinalOrderNotificationFinalOrderOverdueState() {
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldUpdateCaseDataAndStateWhenAboutToSubmitIsCalled() {
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();
        caseDetails.setState(AwaitingFinalOrder);

        final CaseDetails<CaseData, State> updatedCaseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData)
            .build();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(FinalOrderRequested);

        when(applyForFinalOrderService.applyForFinalOrderAsApplicant2(caseDetails)).thenReturn(updatedCaseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            applicant2ApplyForFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        verify(applyForFinalOrderService).applyForFinalOrderAsApplicant2(caseDetails);
        assertThat(aboutToSubmitResponse.getData().getApplication().getPreviousState()).isEqualTo(AwaitingFinalOrder);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldReturnErrorCallbackIfValidateApplyForFinalOrderFails() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();
        caseDetails.setState(AwaitingFinalOrder);

        final List<String> errors = new ArrayList<>();
        errors.add("Test error app2");

        when(applyForFinalOrderService.validateApplyForFinalOrder(caseData, true)).thenReturn(errors);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            applicant2ApplyForFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(aboutToSubmitResponse.getErrors()).isNotEmpty();
    }

    @Test
    void shouldPassJointCaseDetailsToGeneralReferralService() {
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verify(generalReferralService).caseWorkerGeneralReferral(same(caseDetails));
    }

    @Test
    void shouldNotPassSoleCaseDetailsToGeneralReferralService() {
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        applicant2ApplyForFinalOrder.submitted(caseDetails, null);

        verify(generalReferralService, never()).caseWorkerGeneralReferral(same(caseDetails));
    }
}
