package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponsePartnerNewEmailOrPostalAddress implements HasLabel {
    @JsonProperty("newPostalAddress")
    NEW_POSTAL_ADDRESS("I have a new postal address"),

    @JsonProperty("newEmailAddress")
    NEW_EMAIL_ADDRESS("I have a new email address"),

    @JsonProperty("newEmailAndPostalAddress")
    NEW_EMAIL_AND_POSTAL_ADDRESS("I have a new email address and postal address"),

    @JsonProperty("contactDetailsUpdated")
    CONTACT_DETAILS_UPDATED("Contact details have been updated");

    private final String label;
}
