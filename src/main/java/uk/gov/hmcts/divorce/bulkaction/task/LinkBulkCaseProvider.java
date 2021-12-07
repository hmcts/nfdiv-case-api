package uk.gov.hmcts.divorce.bulkaction.task;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;

public class LinkBulkCaseProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_LINK_WITH_BULK_CASE;
    }

    @Override
    public CaseTask getCaseTask(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {
        return mainCaseDetails -> {
            mainCaseDetails.getData().setBulkListCaseReference(String.valueOf(bulkCaseDetails.getId()));
            return mainCaseDetails;
        };
    }
}
