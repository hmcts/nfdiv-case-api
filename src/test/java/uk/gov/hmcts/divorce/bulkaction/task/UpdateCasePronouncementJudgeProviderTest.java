package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UpdateCasePronouncementJudgeProviderTest {

    @InjectMocks
    private UpdateCasePronouncementJudgeProvider updateCasePronouncementJudgeProvider;

    @Test
    void shouldReturnSystemUpdateCasePronouncementJudgeCaseTask() {

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .pronouncementJudge("The Judge")
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setData(bulkActionCaseData);

        final var caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = updateCasePronouncementJudgeProvider.getCaseTask(bulkCaseDetails);

        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);
        final ConditionalOrder resultConditionalOrder = resultCaseDetails.getData().getConditionalOrder();

        assertThat(resultConditionalOrder.getPronouncementJudge()).isEqualTo("The Judge");
    }
}