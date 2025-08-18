package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponsePartnerNewEmailOrAddress implements HasLabel {
    @JsonProperty("address")
    ADDRESS("I have a new postal address"),

    @JsonProperty("email")
    EMAIL("I have a new email address"),

    @JsonProperty("emailAndAddress")
    EMAIL_AND_ADDRESS("I have a new email address and postal address");

    private final String label;
}

