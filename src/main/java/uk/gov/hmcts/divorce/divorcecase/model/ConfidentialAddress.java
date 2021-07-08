package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ConfidentialAddress implements HasLabel {

    @JsonProperty("share")
    SHARE("-"),

    @JsonProperty("keep")
    KEEP("Confidential Address");

    private final String label;
}
