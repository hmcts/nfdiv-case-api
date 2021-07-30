package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectReason {

    @CCD(
        label = "Reject reason",
        typeOverride = FixedList,
        typeParameterOverride = "RejectReasonType"
    )
    private RejectReasonType rejectReasonType;

    @CCD(
        label = "Reject details",
        typeOverride = TextArea
    )
    private String rejectDetails;
}
