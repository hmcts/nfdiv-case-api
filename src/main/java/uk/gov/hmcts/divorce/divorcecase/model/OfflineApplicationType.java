package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum OfflineApplicationType implements HasLabel {

    @JsonProperty("sole")
    SOLE("Sole"),

    @JsonProperty("joint")
    JOINT("Joint"),

    @JsonProperty("switchToSole")
    SWITCH_TO_SOLE("Switch to sole");

    private final String label;
}
