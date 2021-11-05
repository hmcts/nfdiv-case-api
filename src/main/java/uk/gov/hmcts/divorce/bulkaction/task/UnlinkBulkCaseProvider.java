package uk.gov.hmcts.divorce.bulkaction.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;

@Component
public class UnlinkBulkCaseProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_REMOVE_BULK_CASE;
    }

    @Override
    public CaseTask getCaseTask(final BulkActionCaseData bulkActionCaseData) {
        return mainCaseDetails -> {
            mainCaseDetails.getData().setBulkListCaseReference(EMPTY);
            return mainCaseDetails;
        };
    }
}
