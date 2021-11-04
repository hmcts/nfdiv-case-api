package uk.gov.hmcts.divorce.bulkaction.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemUnlinkBulkCase.SYSTEM_UNLINK_BULK_CASE;

@Component
public class UnlinkBulkCaseProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_UNLINK_BULK_CASE;
    }

    @Override
    public CaseTask getCaseTask(final BulkActionCaseData bulkActionCaseData) {
        return mainCaseDetails -> {
            mainCaseDetails.getData().setBulkListCaseReference(null);
            return mainCaseDetails;
        };
    }
}
