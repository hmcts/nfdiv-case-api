package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum JurisdictionConnections implements HasLabel {


    @JsonProperty("J")
    @JsonPropertyDescription("APP_1_RESIDENT_JOINT")
    APP_1_RESIDENT_JOINT("The applicant habitually resides in England and Wales (Joint Application Only)"),

    @JsonProperty("A")
    @JsonPropertyDescription("APP_1_APP_2_RESIDENT")
    APP_1_APP_2_RESIDENT("The applicant and the respondent are habitually resident in England and Wales"),

    @JsonProperty("B")
    @JsonPropertyDescription("APP_1_APP_2_LAST_RESIDENT")
    APP_1_APP_2_LAST_RESIDENT("The applicant and respondent were last habitually resident in England and Wales "
        + "and one of them still resides there"),

    @JsonProperty("C")
    @JsonPropertyDescription("APP_2_RESIDENT")
    APP_2_RESIDENT("The respondent is habitually resident in England and Wales"),

    @JsonProperty("D")
    @JsonPropertyDescription("APP_1_RESIDENT_TWELVE_MONTHS")
    APP_1_RESIDENT_TWELVE_MONTHS("The applicant is habitually resident in England and Wales and has resided there "
        + "for at least a year immediately prior to the presentation of the application"),

    @JsonProperty("E")
    @JsonPropertyDescription("APP_1_RESIDENT_SIX_MONTHS")
    APP_1_RESIDENT_SIX_MONTHS("The applicant is domiciled and habitually resident in England and Wales and has "
        + "resided there for at least six months immediately prior to the application"),

    @JsonProperty("F")
    @JsonPropertyDescription("APP_1_APP_2_DOMICILED")
    APP_1_APP_2_DOMICILED("The applicant and respondent are both domiciled in England and Wales"),

    @JsonProperty("G")
    @JsonPropertyDescription("APP_1_DOMICILED")
    APP_1_DOMICILED("The applicant is domiciled in England and Wales"),

    @JsonProperty("H")
    @JsonPropertyDescription("APP_2_DOMICILED")
    APP_2_DOMICILED("The respondent is domiciled in England and Wales"),

    @JsonProperty("I")
    @JsonPropertyDescription("RESIDUAL_JURISDICTION")
    RESIDUAL_JURISDICTION("The Applicant and Respondent registered as civil partners of each other in "
        + "England or Wales or, in the case of a same sex couple, married each other under "
        + "the law of England and Wales and it would be in the interests of justice for "
        + "the court to assume jurisdiction in this case (same sex married couples only)");


    private final String label;
}
