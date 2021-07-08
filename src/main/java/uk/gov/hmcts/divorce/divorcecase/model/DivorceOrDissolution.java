package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DivorceOrDissolution implements HasLabel {

    @JsonProperty("divorce")
    DIVORCE("Divorce"),

    @JsonProperty("dissolution")
    DISSOLUTION("Dissolution");

    private final String label;

    public boolean isDivorce() {
        return DIVORCE.name().equalsIgnoreCase(this.name());
    }
}
