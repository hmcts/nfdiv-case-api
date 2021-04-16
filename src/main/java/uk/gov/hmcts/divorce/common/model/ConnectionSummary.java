package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ConnectionSummary implements HasLabel {

    @JsonProperty("YES")
    YES("Yes"),

    @JsonProperty("NO")
    NO("No"),

    @JsonProperty("MANUAL")
    MANUAL("Manual");

    private final String label;
}
