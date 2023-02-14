package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.task.ProgressFinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant1;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant2;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyForFinalOrderServiceTest {

    @InjectMocks
    private ApplyForFinalOrderService applyForFinalOrderService;

    @Mock
    private SetFinalOrderFieldsAsApplicant1 setFinalOrderFieldsAsApplicant1;

    @Mock
    private SetFinalOrderFieldsAsApplicant2 setFinalOrderFieldsAsApplicant2;

    @Mock
    private ProgressFinalOrderState progressFinalOrderState;

    @Test
    void shouldRunCorrectTasksForApplyForFinalOrderAsApplicant1() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setFinalOrderFieldsAsApplicant1.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(progressFinalOrderState.apply(caseDetails)).thenReturn(expectedCaseDetails);

        applyForFinalOrderService.applyForFinalOrderAsApplicant1(caseDetails);

        verify(setFinalOrderFieldsAsApplicant1).apply(caseDetails);
        verify(progressFinalOrderState).apply(caseDetails);
    }

    @Test
    void shouldRunCorrectTasksForApplyForFinalOrderAsApplicant2() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setFinalOrderFieldsAsApplicant2.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(progressFinalOrderState.apply(caseDetails)).thenReturn(expectedCaseDetails);

        applyForFinalOrderService.applyForFinalOrderAsApplicant2(caseDetails);

        verify(setFinalOrderFieldsAsApplicant2).apply(caseDetails);
        verify(progressFinalOrderState).apply(caseDetails);
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
}
