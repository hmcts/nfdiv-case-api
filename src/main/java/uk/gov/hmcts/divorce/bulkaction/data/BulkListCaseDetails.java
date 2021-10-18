package uk.gov.hmcts.divorce.bulkaction.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;

import java.time.LocalDate;

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
    private CaseLink caseReference;

    @CCD(
        label = "Legal advisor decision date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate decisionDate;
}
