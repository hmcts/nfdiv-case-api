package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum LegalConnections implements HasLabel {

    @JsonProperty("A")
    COURTS_RESIDUAL_JURISDICTION("The Applicant and Respondent registered as civil partners of each other in "
        + "England or Wales or, in the case of a same sex couple, married each other under "
        + "the law of England and Wales and it would be in the interests of justice for "
        + "the court to assume jurisdiction in this case (same sex married couples only)"),

    @JsonProperty("B")
    RESPONDENT_DOMICILED("The respondent is domiciled in England and Wales"),

    @JsonProperty("C")
    APPLICANT_DOMICILED("The applicant is domiciled in England and Wales"),

    @JsonProperty("D")
    APPLICANT_RESPONDENT_DOMICILED("The applicant and respondent are both domiciled in England and Wales"),

    @JsonProperty("E")
    APPLICANT_DOMICILED_RESIDENT("The applicant is domiciled and habitually resident in England and Wales and has resided "
        + "there for at least six months immediately prior to the application"),

    @JsonProperty("F")
    APPLICANT_RESIDENT("The applicant is habitually resident in England and Wales and has resided there for at least a year "
        + "immediately prior to the presentation of the application"),

    @JsonProperty("G")
    RESPONDENT_RESIDENT("The respondent is habitually resident in England and Wales"),

    @JsonProperty("H")
    APPLICANT_RESPONDENT_ONE_RESIDENT("The applicant and respondent were last habitually resident in England and Wales and one "
        + "of them still resides there"),

    @JsonProperty("I")
    APPLICANT_RESPONDENT_RESIDENT("The applicant and the respondent are habitually resident in England and Wales");

    private final String label;
}
