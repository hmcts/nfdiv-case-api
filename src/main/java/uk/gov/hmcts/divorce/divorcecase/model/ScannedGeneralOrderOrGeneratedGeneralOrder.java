package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ScannedGeneralOrderOrGeneratedGeneralOrder implements HasLabel {

    @JsonProperty("scanned")
    SCANNED_GENERAL_ORDER("Scanned"),

    @JsonProperty("generated")
    GENERATED_GENERAL_ORDER("Generated");

    private final String label;

    public boolean isScannedGeneralOrder() {
        return SCANNED_GENERAL_ORDER.name().equalsIgnoreCase(this.name());
    }
}
