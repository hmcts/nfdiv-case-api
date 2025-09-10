package uk.gov.hmcts.divorce.divorcecase.model.interimapplications;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponseNoNewAddressDetails implements HasLabel {
    @JsonProperty("inPersonService")
    IN_PERSON_SERVICE("I want to arrange for in person service"),

    @JsonProperty("alternativeService")
    ALTERNATIVE_SERVICE("I want to apply for alternative service"),

    @JsonProperty("noContactDetails")
    NO_CONTACT_DETAILS("I do not have any other way to contact them");

    private final String label;
}
