package uk.gov.hmcts.divorce.bulkaction.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;

@Getter
@AllArgsConstructor
public class BulkCaseElement implements HasLabel {

    private final CaseLink value;
    private final String label;
}
