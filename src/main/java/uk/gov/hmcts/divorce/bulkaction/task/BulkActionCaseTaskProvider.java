package uk.gov.hmcts.divorce.bulkaction.task;

import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

public interface BulkActionCaseTaskProvider {

    String getEventId();

    CaseTask getCaseTask(final BulkActionCaseData bulkActionCaseData);
}
