package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetHoldingDueDateTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @InjectMocks
    private SetHoldingDueDate setHoldingDueDate;

    @Test
    void shouldSetHoldingDueDate() {

        final long caseId = TEST_CASE_ID;
        final LocalDate issueDate = getExpectedLocalDate();
        final LocalDate holdingDueDate = issueDate.plusDays(141);

        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(issueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(holdingPeriodService.getDueDateFor(issueDate)).thenReturn(holdingDueDate);

        final CaseDetails<CaseData, State> result = setHoldingDueDate.apply(caseDetails);

        assertThat(result.getData().getDueDate()).isEqualTo(holdingDueDate);
        verify(holdingPeriodService).getDueDateFor(issueDate);
    }
}
