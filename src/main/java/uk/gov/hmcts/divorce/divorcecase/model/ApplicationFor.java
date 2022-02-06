package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ApplicationFor implements HasLabel {

    @JsonProperty("marriageDissolved")
    MARRIAGE_DISSOLVED("That the marriage be dissolved"),

    @JsonProperty("civilPartnershipDissolved")
    CIVIL_PARTNERSHIP_DISSOLVED("That the civil partnership be dissolved");

    private final String label;
}
