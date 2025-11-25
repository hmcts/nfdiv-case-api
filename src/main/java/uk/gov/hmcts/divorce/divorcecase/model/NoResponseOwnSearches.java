package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponseOwnSearches implements HasLabel {
    @JsonProperty("yes")
    YES("I have already tried to find my partner's contact details"),

    @JsonProperty("no")
    NO("I have not tried to find my partner's contact details"),

    @JsonProperty("notFound")
    NOT_FOUND("I've not been able to find contact details for my partner");

    private final String label;
}
