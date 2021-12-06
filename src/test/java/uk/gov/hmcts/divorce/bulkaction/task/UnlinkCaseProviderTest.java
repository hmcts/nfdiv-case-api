package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UnlinkCaseProviderTest {

    @InjectMocks
    private UnlinkBulkCaseProvider unlinkBulkCaseProvider;

    @Test
    void shouldReturnSystemUnlinkBulkCaseTask() {

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();

        final var caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = unlinkBulkCaseProvider.getCaseTask(bulkCaseDetails);

        final CaseDetails<CaseData, State> unlinkBulkCaseProvider = caseTask.apply(caseDetails);

        assertThat(unlinkBulkCaseProvider.getData().getBulkListCaseReference()).isEmpty();
    }
}
