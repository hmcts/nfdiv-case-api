package uk.gov.hmcts.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum JurisdictionConnections implements HasLabel {
    @JsonProperty("A")
    @JsonPropertyDescription("The Applicant and Co-Applicant are habitually resident")
    APP_COAPP_RESIDENT("A"),

    @JsonProperty("B")
    @JsonPropertyDescription("The Applicant and Co-Applicant were last habitually resident in England and Wales")
    APP_COAPP_LAST_RESIDENT("B"),

    @JsonProperty("C")
    @JsonPropertyDescription("The Applicant habitually resides in England and Wales")
    APP_RESIDENT("C"),

    @JsonProperty("D")
    @JsonPropertyDescription("The Applicant is habitually resident in England and Wales and has been for 12 months")
    APP_RESIDENT_TWELVE_MONTHS("D"),

    @JsonProperty("E")
    @JsonPropertyDescription("The Applicant is habitually resident in England and Wales and has been for 6 months")
    APP_RESIDENT_SIX_MONTHS("E"),

    @JsonProperty("F")
    @JsonPropertyDescription("The Applicant and Co-Applicant are both domiciled in England and Wales")
    APP_COAPP_DOMICILED("F"),

    @JsonProperty("G")
    @JsonPropertyDescription("Eligible for Residual Jurisdiction")
    RESIDUAL_JURISDICTION("G");

    private final String label;
}
