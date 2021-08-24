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
    @JsonPropertyDescription("The Applicant and Respondent registered as civil partners of each other in "
        + "England or Wales or, in the case of a same sex couple, married each other under "
        + "the law of England and Wales and it would be in the interests of justice for "
        + "the court to assume jurisdiction in this case (same sex married couples only)")
    RESIDUAL_JURISDICTION("A"),

    @JsonProperty("B")
    @JsonPropertyDescription("The respondent is domiciled in England and Wales")
    APP_2_DOMICILED("B"),

    @JsonProperty("C")
    @JsonPropertyDescription("The applicant is domiciled in England and Wales")
    APP_1_DOMICILED("C"),

    @JsonProperty("D")
    @JsonPropertyDescription("The applicant and respondent are both domiciled in England and Wales")
    APP_1_APP_2_DOMICILED("D"),

    @JsonProperty("E")
    @JsonPropertyDescription("The applicant is domiciled and habitually resident in England and Wales and has "
        + "resided there for at least six months immediately prior to the application")
    APP_1_RESIDENT_SIX_MONTHS("E"),

    @JsonProperty("F")
    @JsonPropertyDescription("The applicant is habitually resident in England and Wales and has resided there "
        + "for at least a year immediately prior to the presentation of the application")
    APP_1_RESIDENT_TWELVE_MONTHS("F"),

    @JsonProperty("G")
    @JsonPropertyDescription("The respondent is habitually resident in England and Wales")
    APP_2_RESIDENT("G"),

    @JsonProperty("H")
    @JsonPropertyDescription("The applicant and respondent were last habitually resident in England and Wales "
        + "and one of them still resides there")
    APP_1_APP_2_LAST_RESIDENT("H"),

    @JsonProperty("I")
    @JsonPropertyDescription("The applicant and the respondent are habitually resident in England and Wales")
    APP_1_APP_2_RESIDENT("I");

    /*
    @JsonProperty("J")
    @JsonPropertyDescription("The applicant habitually resides in England and Wales")
    APP_1_RESIDENT_JOINT("J");
    */
    private final String label;
}
