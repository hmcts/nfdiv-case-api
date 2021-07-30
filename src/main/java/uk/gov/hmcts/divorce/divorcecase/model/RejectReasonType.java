package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RejectReasonType implements HasLabel {
    @JsonProperty("noInfo")
    NO_INFO("No information"),

    @JsonProperty("incorrectInfo")
    INCORRECT_INFO("Incorrect information"),

    @JsonProperty("Other")
    OTHER("Other");

    private final String label;
}
