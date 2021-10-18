package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum JudgeCostsClaimGranted implements HasLabel {

    @JsonProperty("Yes")
    YES("Yes"),

    @JsonProperty("No")
    NO("No"),

    @JsonProperty("Adjourn")
    ADJOURN("Adjourn");

    private final String label;
}
