package uk.gov.hmcts.divorce.api.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum JurisdictionConnections implements HasLabel {
    @JsonProperty("A")
    @JsonPropertyDescription("The Petitioner and Respondent are habitually resident")
    PET_RESP_RESIDENT("A"),

    @JsonProperty("B")
    @JsonPropertyDescription("The Petitioner and Respondent were last habitually resident in England and Wales")
    PET_RESP_LAST_RESIDENT("B"),

    @JsonProperty("C")
    @JsonPropertyDescription("The Respondent habitually resides in England and Wales")
    RESP_RESIDENT("C"),

    @JsonProperty("D")
    @JsonPropertyDescription("The Petitioner is habitually resident in England and Wales and has been for 12 months")
    PET_RESIDENT_TWELVE_MONTHS("D"),

    @JsonProperty("E")
    @JsonPropertyDescription("The Petitioner is habitually resident in England and Wales and has been for 6 months")
    PET_RESIDENT_SIX_MONTHS("E"),

    @JsonProperty("F")
    @JsonPropertyDescription("The Petitioner and Respondent are both domiciled in England and Wales")
    PET_RESP_DOMICILED("F"),

    @JsonProperty("G")
    @JsonPropertyDescription("Eligible for Residual Jurisdiction")
    RESIDUAL_JURISDICTION("G");

    private final String label;
}
