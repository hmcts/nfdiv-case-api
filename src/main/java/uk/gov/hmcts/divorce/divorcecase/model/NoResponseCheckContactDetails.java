package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponseCheckContactDetails implements HasLabel {
    @JsonProperty("upToDate")
    UP_TO_DATE("yes, these details are up to date"),

    @JsonProperty("newAddress")
    NEW_ADDRESS("I have a new postal or email address for my partner"),

    @JsonProperty("notKnown")
    NOT_KNOWN("Not known");

    private final String label;
}
