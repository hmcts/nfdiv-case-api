package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum MarriageFormation implements HasLabel {

    @JsonProperty("sameSexCouple")
    SAME_SEX_COUPLE("Same-sex couple"),

    @JsonProperty("oppositeSexCouple")
    OPPOSITE_SEX_COUPLE("Opposite-sex couple");

    private final String label;
}
