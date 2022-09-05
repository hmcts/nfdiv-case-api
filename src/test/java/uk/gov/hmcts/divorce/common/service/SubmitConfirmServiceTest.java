package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.SetServiceConfirmed;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.task.SetConfirmServiceDueDate;
import uk.gov.hmcts.divorce.solicitor.service.task.SetConfirmServiceState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getSolicitorService;

@ExtendWith(MockitoExtension.class)
public class SubmitConfirmServiceTest {

    @Mock
    private SetConfirmServiceDueDate setConfirmServiceDueDate;

    @Mock
    private SetServiceConfirmed setServiceConfirmed;

    @Mock
    private SetConfirmServiceState setConfirmServiceState;

    @InjectMocks
    private SubmitConfirmService submitConfirmService;

    @Test
    void shouldOnlyUpdateConfirmServiceDueDate() {

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();

        final var caseData = caseData();
        final var updatedCaseData = caseData();

        caseData.getApplication().setSolicitorService(getSolicitorService());
        updatedCaseData.getApplication().setSolicitorService(getSolicitorService());
        updatedCaseData.setDueDate(getExpectedLocalDate().plusDays(14));

        caseDetails.setData(caseData);
        updatedCaseDetails.setData(updatedCaseData);

        when(setConfirmServiceDueDate.apply(caseDetails)).thenReturn(updatedCaseDetails);
        when(setServiceConfirmed.apply(updatedCaseDetails)).thenReturn(updatedCaseDetails);
        when(setConfirmServiceState.apply(updatedCaseDetails)).thenReturn(updatedCaseDetails);

        final CaseDetails<CaseData, State> result = submitConfirmService.submitConfirmService(caseDetails);

        assertThat(result.getData().getDueDate()).isNotEqualTo(caseDetails.getData().getDueDate());
        assertThat(result.getData().getApplication().getSolicitorService()).isEqualTo(
            caseDetails.getData().getApplication().getSolicitorService());

        verify(setConfirmServiceDueDate).apply(caseDetails);
    }
}
