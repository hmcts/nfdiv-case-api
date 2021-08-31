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
    @JsonPropertyDescription("The applicant habitually resides in England and Wales")
    APP_1_RESIDENT_JOINT("J"),

    @JsonProperty("A")
    @JsonPropertyDescription("The applicant and the respondent are habitually resident in England and Wales")
    APP_1_APP_2_RESIDENT("A"),

    @JsonProperty("B")
    @JsonPropertyDescription("The applicant and respondent were last habitually resident in England and Wales "
        + "and one of them still resides there")
    APP_1_APP_2_LAST_RESIDENT("B"),

    @JsonProperty("C")
    @JsonPropertyDescription("The respondent is habitually resident in England and Wales")
    APP_2_RESIDENT("C"),

    @JsonProperty("D")
    @JsonPropertyDescription("The applicant is habitually resident in England and Wales and has resided there "
        + "for at least a year immediately prior to the presentation of the application")
    APP_1_RESIDENT_TWELVE_MONTHS("D"),

    @JsonProperty("E")
    @JsonPropertyDescription("The applicant is domiciled and habitually resident in England and Wales and has "
        + "resided there for at least six months immediately prior to the application")
    APP_1_RESIDENT_SIX_MONTHS("E"),

    @JsonProperty("F")
    @JsonPropertyDescription("The applicant and respondent are both domiciled in England and Wales")
    APP_1_APP_2_DOMICILED("F"),

    @JsonProperty("G")
    @JsonPropertyDescription("The applicant is domiciled in England and Wales")
    APP_1_DOMICILED("G"),

    @JsonProperty("H")
    @JsonPropertyDescription("The respondent is domiciled in England and Wales")
    APP_2_DOMICILED("H"),

    @JsonProperty("I")
    @JsonPropertyDescription("The Applicant and Respondent registered as civil partners of each other in "
        + "England or Wales or, in the case of a same sex couple, married each other under "
        + "the law of England and Wales and it would be in the interests of justice for "
        + "the court to assume jurisdiction in this case (same sex married couples only)")
    RESIDUAL_JURISDICTION("I");

    private final String label;
}
