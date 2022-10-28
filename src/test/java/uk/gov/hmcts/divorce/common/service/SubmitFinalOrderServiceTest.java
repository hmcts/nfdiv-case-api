package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.ProgressFinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant1;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant2;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitFinalOrderServiceTest {

    @InjectMocks
    private SubmitFinalOrderService submitFinalOrderService;

    @Mock
    private SetFinalOrderFieldsAsApplicant1 setFinalOrderFieldsAsApplicant1;

    @Mock
    private SetFinalOrderFieldsAsApplicant2 setFinalOrderFieldsAsApplicant2;

    @Mock
    private ProgressFinalOrderState progressFinalOrderState;

    @Test
    void submitFinalOrderAsApplicant1ShouldRunTheCorrectTasks() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setFinalOrderFieldsAsApplicant1.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(progressFinalOrderState.apply(caseDetails)).thenReturn(expectedCaseDetails);

        submitFinalOrderService.submitFinalOrderAsApplicant1(caseDetails);

        verify(setFinalOrderFieldsAsApplicant1).apply(caseDetails);
        verify(progressFinalOrderState).apply(caseDetails);
    }

    @Test
    void submitFinalOrderAsApplicant2ShouldRunTheCorrectTasks() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setFinalOrderFieldsAsApplicant2.apply(caseDetails)).thenReturn(expectedCaseDetails);
        when(progressFinalOrderState.apply(caseDetails)).thenReturn(expectedCaseDetails);

        submitFinalOrderService.submitFinalOrderAsApplicant2(caseDetails);

        verify(setFinalOrderFieldsAsApplicant2).apply(caseDetails);
        verify(progressFinalOrderState).apply(caseDetails);
    }
}
