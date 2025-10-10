package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesOrNoOrNotKnown {
    @JsonProperty("Yes")
    YES("Yes"),

    @JsonProperty("No")
    NO("No"),

    @JsonProperty("NotKnown")
    NOT_KNOWN("Not known");

    private final String value;

    public boolean toBoolean() {
        return YES.name().equalsIgnoreCase(this.name());
    }
}
