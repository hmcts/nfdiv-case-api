package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.Applicant2AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.service.task.ProgressApplicant1FinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.ProgressApplicant2FinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant1;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant2;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant2Sol;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ApplyForFinalOrderServiceTest {

    @InjectMocks
    private ApplyForFinalOrderService applyForFinalOrderService;

    @Mock
    private SetFinalOrderFieldsAsApplicant1 setFinalOrderFieldsAsApplicant1;

    @Mock
    private SetFinalOrderFieldsAsApplicant2 setFinalOrderFieldsAsApplicant2;

    @Mock
    private SetFinalOrderFieldsAsApplicant2Sol setFinalOrderFieldsAsApplicant2Sol;

    @Mock
    private ProgressApplicant1FinalOrderState progressApplicant1FinalOrderState;

    @Mock
    private ProgressApplicant2FinalOrderState progressApplicant2FinalOrderState;

    @Mock
    private Applicant2AppliedForFinalOrderNotification applicant2AppliedForFinalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Test
    void shouldRunCorrectTasksForApplyForFinalOrderAsApplicant1() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setFinalOrderFieldsAsApplicant1.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(progressApplicant1FinalOrderState.apply(caseDetails)).thenReturn(expectedCaseDetails);

        applyForFinalOrderService.applyForFinalOrderAsApplicant1(caseDetails);

        verify(setFinalOrderFieldsAsApplicant1).apply(caseDetails);
        verify(progressApplicant1FinalOrderState).apply(caseDetails);
    }

    @Test
    void shouldRunCorrectTasksForApplyForFinalOrderAsApplicant2() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setFinalOrderFieldsAsApplicant2.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(progressApplicant2FinalOrderState.apply(caseDetails)).thenReturn(expectedCaseDetails);

        applyForFinalOrderService.applyForFinalOrderAsApplicant2(caseDetails);

        verify(setFinalOrderFieldsAsApplicant2).apply(caseDetails);
        verify(progressApplicant2FinalOrderState).apply(caseDetails);
    }

    @Test
    void shouldRunCorrectTasksForApplyForFinalOrderAsApplicant2Sol() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setFinalOrderFieldsAsApplicant2Sol.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(progressApplicant2FinalOrderState.apply(caseDetails)).thenReturn(expectedCaseDetails);

        applyForFinalOrderService.applyForFinalOrderAsApplicant2Sol(caseDetails);

        verify(setFinalOrderFieldsAsApplicant2Sol).apply(caseDetails);
        verify(progressApplicant2FinalOrderState).apply(caseDetails);
    }

    @Test
    void shouldAddErrorWhenApplicant1HasAlreadyAppliedForFinalOrder() {
        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .finalOrder(FinalOrder.builder().applicant1AppliedForFinalOrderFirst(YesOrNo.YES).build())
            .build();

        List<String> errors = applyForFinalOrderService.validateApplyForFinalOrder(caseData, false);

        assertThat(errors).contains("Applicant / Applicant 1 has already applied for final order.");
        assertThat(errors).hasSize(1);
    }

    @Test
    void shouldAddErrorWhenApplicant2HasAlreadyAppliedForFinalOrder() {
        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .finalOrder(FinalOrder.builder().applicant2AppliedForFinalOrderFirst(YesOrNo.YES).build())
            .build();

        List<String> errors = applyForFinalOrderService.validateApplyForFinalOrder(caseData, true);

        assertThat(errors).contains("Applicant 2 has already applied for final order.");
        assertThat(errors).hasSize(1);
    }

    @Test
    void shouldNotAddErrorWhenNobodyHasAlreadyAppliedForFinalOrderYet() {
        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .build();

        List<String> errors = applyForFinalOrderService.validateApplyForFinalOrder(caseData, false);

        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldSendNotificationsByDelegatingToNotificationDispatcher() {
        var details = new CaseDetails<CaseData, State>();
        details.setId(TEST_CASE_ID);

        applyForFinalOrderService.sendRespondentAppliedForFinalOrderNotifications(details);

        verify(notificationDispatcher).send(applicant2AppliedForFinalOrderNotification, details.getData(), TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
