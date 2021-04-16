package uk.gov.hmcts.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum WhoDivorcing implements HasLabel {

    @JsonProperty("husband")
    HUSBAND("Husband"),

    @JsonProperty("wife")
    WIFE("Wife");

    private final String label;
}
