package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum JurisdictionConnections implements HasLabel {
    @JsonProperty("A")
    @JsonPropertyDescription("The applicant and the respondent are habitually resident")
    APP_1_APP_2_RESIDENT("A"),

    @JsonProperty("B")
    @JsonPropertyDescription("The applicant and the respondent were last habitually resident in England and Wales")
    APP_1_APP_2_LAST_RESIDENT("B"),

    @JsonProperty("C")
    @JsonPropertyDescription("The respondent habitually resides in England and Wales")
    APP_2_RESIDENT("C"),

    @JsonProperty("D")
    @JsonPropertyDescription("The applicant is habitually resident in England and Wales and has been for 12 months")
    APP_1_RESIDENT_TWELVE_MONTHS("D"),

    @JsonProperty("E")
    @JsonPropertyDescription("The applicant is habitually resident in England and Wales and has been for 6 months")
    APP_1_RESIDENT_SIX_MONTHS("E"),

    @JsonProperty("F")
    @JsonPropertyDescription("The applicant and the respondent are both domiciled in England and Wales")
    APP_1_APP_2_DOMICILED("F"),

    @JsonProperty("G")
    @JsonPropertyDescription("Eligible for Residual Jurisdiction")
    RESIDUAL_JURISDICTION("G"),

    @JsonProperty("H")
    @JsonPropertyDescription("The applicant is domiciled in England and Wales")
    APP_1_DOMICILED("H"),

    @JsonProperty("I")
    @JsonPropertyDescription("The respondent is domiciled in England and Wales")
    APP_2_DOMICILED("I"),

    @JsonProperty("J")
    @JsonPropertyDescription("The applicant habitually resides in England and Wales")
    APP_1_RESIDENT_JOINT("J");

    private final String label;
}
