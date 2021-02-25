package uk.gov.hmcts.reform.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
@AllArgsConstructor
public enum RejectReasonList implements HasLabel {

    @JsonProperty("RejectReasonNoInfo")
    REJECT_REASON_NO_INFO("No information"),

    @JsonProperty("RejectReasonIncorrectInfo")
    REJECT_REASON_INCORRECT_INFO("Incorrect information"),

    @JsonProperty("RejectReasonOther")
    REJECT_REASON_OTHER("Other");

    private final String label;
}
