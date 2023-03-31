package uk.gov.hmcts.divorce.bulkaction.task;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;

import java.util.function.Function;

public interface BulkCaseTask extends
    Function<CaseDetails<BulkActionCaseData, BulkActionState>, CaseDetails<BulkActionCaseData, BulkActionState>> {
}
