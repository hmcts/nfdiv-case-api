package uk.gov.hmcts.reform.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
@AllArgsConstructor
public enum WhoDivorcingEnum implements HasLabel {

    @JsonProperty("husband")
    HUSBAND("Husband"),

    @JsonProperty("wife")
    WIFE("Wife");

    private final String label;
}
