package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ContactDetailsType implements HasLabel {

    @JsonProperty("private")
    PRIVATE("private", "Keep contact details private "),

    @JsonProperty("public")
    PUBLIC("public", "Do not need to keep contact details private");

    private String type;
    private final String label;
}
