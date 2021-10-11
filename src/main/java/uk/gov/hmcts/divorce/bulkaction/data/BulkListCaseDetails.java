package uk.gov.hmcts.divorce.bulkaction.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.CaseLink;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkListCaseDetails {
    @CCD(
        label = "Case parties"
    )
    private String caseParties;

    @CCD(
        label = "Case reference",
        typeOverride = CaseLink
    )
    private uk.gov.hmcts.ccd.sdk.type.CaseLink caseReference;
}
