package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RemoveCaseFromBulkListProviderTest {

    @InjectMocks
    private RemoveCaseFromBulkListProvider removeCaseFromBulkListProvider;

    @Test
    void shouldReturnSystemPronounceCaseTask() {

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .build();

        final var caseData = CaseData.builder()
            .bulkListCaseReference("BulkCaseReference")
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = removeCaseFromBulkListProvider.getCaseTask(bulkActionCaseData);
        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);

        assertThat(resultCaseDetails.getData().getBulkListCaseReference()).isNull();
    }
}
