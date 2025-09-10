package uk.gov.hmcts.divorce.divorcecase.model.interimapplications;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponseSearchOrDispense implements HasLabel {
    @JsonProperty("search")
    SEARCH("I want to ask the court to search government records"),

    @JsonProperty("dispense")
    DISPENSE("I want to apply to proceed without sending papers");

    private final String label;
}
