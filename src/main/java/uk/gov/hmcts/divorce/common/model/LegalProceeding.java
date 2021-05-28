package uk.gov.hmcts.divorce.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LegalProceeding {

    @CCD(
        label = "Case number",
        access = {DefaultAccess.class}
    )
    private String caseNumber;

    @CCD(
        label = "Case relates to",
        typeOverride = FixedList,
        typeParameterOverride = "LegalProceedingsRelated",
        access = {DefaultAccess.class}
    )
    private LegalProceedingsRelated caseRelatesTo;

    @CCD(
        label = "Case details",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String caseDetail;
}
