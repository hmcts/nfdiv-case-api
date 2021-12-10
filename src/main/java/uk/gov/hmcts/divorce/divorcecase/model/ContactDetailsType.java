package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ContactDetailsType implements HasLabel {

    @JsonProperty("private")
    PRIVATE("Keep contact details private "),

    @JsonProperty("public")
    PUBLIC("Do not need to keep contact details private");

    private final String label;
}
