package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ServiceApplicationType implements HasLabel {

    @JsonProperty("deemed")
    DEEMED("Deemed as served"),

    @JsonProperty("dispensed")
    DISPENSED("Dispensed with service"),

    @JsonProperty("bailiff")
    BAILIFF("Bailiff application");

    private final String label;
}
