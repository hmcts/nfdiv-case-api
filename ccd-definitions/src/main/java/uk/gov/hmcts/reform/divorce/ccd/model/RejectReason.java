package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.types.CCD;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.RejectReasonList;

import static uk.gov.hmcts.ccd.sdk.types.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.types.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@ComplexType(generate = true)
public class RejectReason {

    @JsonProperty("RejectReasonType")
    @CCD(
        label = "Reject reason",
        type = FixedList,
        typeParameter = "RejectReasonList"
    )
    private RejectReasonList rejectReasonType;

    @JsonProperty("RejectReasonText")
    @CCD(
        label = "Reject details",
        type = TextArea
    )
    private String rejectReasonText;
}
