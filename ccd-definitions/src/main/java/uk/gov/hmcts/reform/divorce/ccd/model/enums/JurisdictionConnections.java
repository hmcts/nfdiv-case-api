package uk.gov.hmcts.reform.divorce.ccd.model.enums;

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
    A("A"),

    @JsonProperty("B")
    @JsonPropertyDescription("The Petitioner and Respondent were last habitually resident in England and Wales")
    B("B"),

    @JsonProperty("C")
    @JsonPropertyDescription("The Respondent habitually resides in England and Wales")
    C("C"),

    @JsonProperty("D")
    @JsonPropertyDescription("The Petitioner is habitually resident in England and Wales and has been for 12 months")
    D("D"),

    @JsonProperty("E")
    @JsonPropertyDescription("The Petitioner is habitually resident in England and Wales and has been for 6 months")
    E("E"),

    @JsonProperty("F")
    @JsonPropertyDescription("The Petitioner and Respondent are both domiciled in England and Wales")
    F("F"),

    @JsonProperty("G")
    @JsonPropertyDescription("Eligible for Residual Jurisdiction")
    G("G");

    private final String label;
}
