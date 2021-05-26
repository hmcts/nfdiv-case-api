package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum JurisdictionConnections implements HasLabel {
    @JsonProperty("A")
    @JsonPropertyDescription("Applicant 1 and Applicant 2 are habitually resident")
    APP_1_APP_2_RESIDENT("A"),

    @JsonProperty("B")
    @JsonPropertyDescription("Applicant 1 and Applicant 2 were last habitually resident in England and Wales")
    APP_1_APP_2_LAST_RESIDENT("B"),

    @JsonProperty("C")
    @JsonPropertyDescription("Applicant 2 habitually resides in England and Wales")
    APP_2_RESIDENT("C"),

    @JsonProperty("D")
    @JsonPropertyDescription("Applicant 1 is habitually resident in England and Wales and has been for 12 months")
    APP_1_RESIDENT_TWELVE_MONTHS("D"),

    @JsonProperty("E")
    @JsonPropertyDescription("Applicant 1 is habitually resident in England and Wales and has been for 6 months")
    APP_1_RESIDENT_SIX_MONTHS("E"),

    @JsonProperty("F")
    @JsonPropertyDescription("Applicant 1 and Applicant 2 are both domiciled in England and Wales")
    APP_1_APP_2_DOMICILED("F"),

    @JsonProperty("G")
    @JsonPropertyDescription("Eligible for Residual Jurisdiction")
    RESIDUAL_JURISDICTION("G"),

    @JsonProperty("H")
    @JsonPropertyDescription("Applicant 1 is domiciled in England and Wales")
    APP_1_DOMICILED("H"),

    @JsonProperty("I")
    @JsonPropertyDescription("Applicant 2 is domiciled in England and Wales")
    APP_2_DOMICILED("I"),

    @JsonProperty("J")
    @JsonPropertyDescription("Applicant 1 habitually resides in England and Wales")
    APP_1_RESIDENT("J");

    private final String label;
}
