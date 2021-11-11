package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMIGHAM;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;

@ExtendWith(MockitoExtension.class)
class UpdateCaseCourtHearingProviderTest {

    @InjectMocks
    private UpdateCaseCourtHearingProvider updateCaseCourtHearingProvider;

    @Test
    void shouldReturnSystemUpdateCaseCourtHearingCaseTask() {

        final var localDateTime = getExpectedLocalDateTime();
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(localDateTime)
            .court(BIRMIGHAM)
            .build();

        final var caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = updateCaseCourtHearingProvider.getCaseTask(bulkActionCaseData);

        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);
        final ConditionalOrder resultConditionalOrder = resultCaseDetails.getData().getConditionalOrder();

        assertThat(resultConditionalOrder.getDateAndTimeOfHearing()).isEqualTo(localDateTime);
        assertThat(resultConditionalOrder.getCourt()).isEqualTo(BIRMIGHAM);
    }
}