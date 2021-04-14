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
    @JsonPropertyDescription("The Applicant and Respondent are habitually resident")
    APP_RESP_RESIDENT("A"),

    @JsonProperty("B")
    @JsonPropertyDescription("The Applicant and Respondent were last habitually resident in England and Wales")
    APP_RESP_LAST_RESIDENT("B"),

    @JsonProperty("C")
    @JsonPropertyDescription("The Respondent habitually resides in England and Wales")
    RESP_RESIDENT("C"),

    @JsonProperty("D")
    @JsonPropertyDescription("The Applicant is habitually resident in England and Wales and has been for 12 months")
    APP_RESIDENT_TWELVE_MONTHS("D"),

    @JsonProperty("E")
    @JsonPropertyDescription("The Applicant is habitually resident in England and Wales and has been for 6 months")
    APP_RESIDENT_SIX_MONTHS("E"),

    @JsonProperty("F")
    @JsonPropertyDescription("The Applicant and Respondent are both domiciled in England and Wales")
    APP_RESP_DOMICILED("F"),

    @JsonProperty("G")
    @JsonPropertyDescription("Eligible for Residual Jurisdiction")
    RESIDUAL_JURISDICTION("G"),

    @JsonProperty("H")
    @JsonPropertyDescription("The Petitioner is domiciled in England and Wales")
    PET_DOMICILED("H"),

    @JsonProperty("I")
    @JsonPropertyDescription("The Respondent is domiciled in England and Wales")
    RESP_DOMICILED("I");

    private final String label;
}
