package uk.gov.hmcts.divorce.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LegalProceeding {

    @CCD(
        label = "Case number"
    )
    private String caseNumber;

    @CCD(
        label = "Case relates to",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentType"
    )
    private Set<LegalProceedingsRelated> caseRelatesTo;

    @CCD(
        label = "Case details",
        typeOverride = TextArea
    )
    private String caseDetail;
}
