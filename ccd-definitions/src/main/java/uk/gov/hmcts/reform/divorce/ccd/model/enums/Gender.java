package uk.gov.hmcts.reform.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
@AllArgsConstructor
public enum Gender implements HasLabel {

    @JsonProperty("male")
    MALE("Male"),

    @JsonProperty("female")
    FEMALE("Female"),

    @JsonProperty("notGiven")
    NOT_GIVEN("Not given");

    private final String label;
}
