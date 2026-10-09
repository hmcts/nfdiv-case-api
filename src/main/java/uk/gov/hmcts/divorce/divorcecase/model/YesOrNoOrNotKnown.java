package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum YesOrNoOrNotKnown {
    @JsonProperty("Yes")
    @CCD(
        label = "Yes"
    )
    YES("Yes"),

    @JsonProperty("No")
    @CCD(
        label = "No"
    )
    NO("No"),

    @JsonProperty("NotKnown")
    @CCD(
        label = "Not known"
    )
    NOT_KNOWN("Not known");

    private final String value;

    public boolean toBoolean() {
        return YES.name().equalsIgnoreCase(this.name());
    }
}
