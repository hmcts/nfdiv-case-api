package uk.gov.hmcts.divorce.bulkaction.service.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;

import java.util.List;

@Data
@AllArgsConstructor
public class CaseFilterProcessingState {

    private List<ListValue<BulkListCaseDetails>> unprocessedCases;
    private List<ListValue<BulkListCaseDetails>> erroredCases;
    private List<ListValue<BulkListCaseDetails>> processedCases;
}
