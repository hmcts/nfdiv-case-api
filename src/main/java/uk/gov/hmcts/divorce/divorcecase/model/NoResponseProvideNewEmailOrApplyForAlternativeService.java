package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponseProvideNewEmailOrApplyForAlternativeService implements HasLabel {
    @JsonProperty("provideNewEmailAddress")
    NEW_ADDRESS("I want to provide a new email address"),

    @JsonProperty("newEmailAddress")
    NEW_EMAIL_ADDRESS("I have a new email address"),

    @JsonProperty("applyForAlternativeService")
    NEW_EMAIL_AND_POSTAL_ADDRESS("I want to apply for alternative service to serve by email only ");

    private final String label;
}
