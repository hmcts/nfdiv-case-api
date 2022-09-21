package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OfflineApplicationType {

    @JsonProperty("sole")
    SOLE("Sole"),

    @JsonProperty("joint")
    JOINT("Joint"),

    @JsonProperty("switchToSole")
    SWITCH_TO_SOLE("Switch to sole");

    private final String label;
}
