package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum Gender implements HasLabel {

    @JsonProperty("male")
    MALE("Male"),

    @JsonProperty("female")
    @JsonAlias({"notGiven"})
    FEMALE("Female");

    private final String label;
}
