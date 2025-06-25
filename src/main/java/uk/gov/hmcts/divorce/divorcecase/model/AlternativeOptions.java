package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum AlternativeOptions implements HasLabel {
    @JsonProperty("byEmail")
    EMAIL("By email"),

    @JsonProperty("inADifferentWay")
    DIFFERENT_WAY("In a different way"),

    @JsonProperty("emailAndDifferentWay")
    EMAIL_AND_DIFFERENT("By both email and a different way");

    private final String label;
}
