package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum MarriageBroken implements HasLabel {

    @JsonProperty("marriageBroken")
    MARRIAGE_BROKEN("The applicant’s marriage has broken down irretrievably");

    private final String label;
}
