package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum Applicant2Represented implements HasLabel {

    @JsonProperty("Yes")
    YES("Yes"),

    @JsonProperty("No")
    NO("No"),

    @JsonProperty("notSure")
    NOT_SURE("I'm not sure");

    private final String label;
}
