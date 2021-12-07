package uk.gov.hmcts.divorce.bulkaction.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
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
    public CaseTask getCaseTask(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {
        return mainCaseDetails -> {
            mainCaseDetails.getData().setBulkListCaseReference(EMPTY);
            return mainCaseDetails;
        };
    }
}
