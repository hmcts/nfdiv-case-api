package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralApplicationFee implements HasLabel {

    @JsonProperty("FEE0227")
    FEE0227("Application on Notice (FEE0227)"),

    @JsonProperty("FEE0228")
    FEE0228("Application without notice (FEE0228)");

    private final String label;
}
