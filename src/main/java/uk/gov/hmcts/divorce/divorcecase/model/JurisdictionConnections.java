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
    @JsonPropertyDescription("APP_1_APP_2_RESIDENT")
    APP_1_APP_2_RESIDENT("Both parties to the marriage or civil partnership are habitually resident in England and Wales"),

    @JsonProperty("B")
    @JsonPropertyDescription("APP_1_APP_2_LAST_RESIDENT")
    APP_1_APP_2_LAST_RESIDENT("Both parties to the marriage or civil partnership were last habitually resident in England and Wales"
        + " and one of them continues to reside there"),

    @JsonProperty("C")
    @JsonPropertyDescription("APP_2_RESIDENT")
    APP_2_RESIDENT("The respondent is habitually resident in England and Wales"),

    @JsonProperty("D")
    @JsonPropertyDescription("APP_1_RESIDENT_TWELVE_MONTHS")
    APP_1_RESIDENT_TWELVE_MONTHS("The applicant is habitually resident in England and Wales and has resided there "
        + "for at least one year immediately before the application was made."),

    @JsonProperty("E")
    @JsonPropertyDescription("APP_1_RESIDENT_SIX_MONTHS")
    APP_1_RESIDENT_SIX_MONTHS("The applicant is domiciled and habitually resident in England and Wales and has resided there "
        + "for at least six months immediately before the application was made"),

    @JsonProperty("F")
    @JsonPropertyDescription("APP_1_APP_2_DOMICILED")
    APP_1_APP_2_DOMICILED("Both parties to the marriage or civil partnership are domiciled in England and Wales"),

    @JsonProperty("G")
    @JsonPropertyDescription("APP_1_DOMICILED")
    APP_1_DOMICILED("Only the applicant is domiciled in England and Wales"),

    @JsonProperty("H")
    @JsonPropertyDescription("APP_2_DOMICILED")
    APP_2_DOMICILED("Only the respondent or applicant 2 is domiciled in England and Wales"),

    @JsonProperty("I")
    @JsonPropertyDescription("RESIDUAL_JURISDICTION")
    RESIDUAL_JURISDICTION("The parties registered as civil partners of each other in England or Wales and it would be in the "
        + "interest of justice for the court to assume jurisdiction in this case. (In same sex civil partnership cases only)"),

    @JsonProperty("J")
    @JsonPropertyDescription("APP_1_RESIDENT_JOINT")
    APP_1_RESIDENT_JOINT("The applicant habitually resides in England and Wales (Joint Application Only)"),

    @JsonProperty("K")
    @JsonPropertyDescription("APP_2_RESIDENT_TWELVE_MONTHS")
    APP_2_RESIDENT_TWELVE_MONTHS("The respondent or applicant 2 is habitually resident in England and Wales and has resided there "
        + "for at least one year immediately before the application was made"),

    @JsonProperty("L")
    @JsonPropertyDescription("APP_2_RESIDENT_SIX_MONTHS")
    APP_2_RESIDENT_SIX_MONTHS("The respondent or applicant 2 is domiciled and habitually resident in England and Wales and "
        + "has resided there for at least six months immediately before the application was made");

    private final String label;
}
