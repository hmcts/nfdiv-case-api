package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum LegalConnections implements HasLabel {

    @JsonProperty("I")
    RESPONDENT_DOMICILED("The respondent is domiciled in England and Wales"),

    @JsonProperty("H")
    APPLICANT_DOMICILED("The applicant is domiciled in England and Wales"),

    @JsonProperty("G")
    COURTS_RESIDUAL_JURISDICTION("The courts of England and Wales have residual jurisdiction (same sex married couple)"),

    @JsonProperty("F")
    APPLICANT_RESPONDENT_DOMICILED("The applicant and respondent are both domiciled in England and Wales"),

    @JsonProperty("E")
    APPLICANT_DOMICILED_RESIDENT("The applicant is domiciled and habitually resident in England and Wales and has resided "
        + "there for at least six months immediately prior to the application"),

    @JsonProperty("D")
    APPLICANT_RESIDENT("The applicant is habitually resident in England and Wales and has resided there for at least a year "
        + "immediately prior to the presentation of the application"),

    @JsonProperty("C")
    RESPONDENT_RESIDENT("The respondent is habitually resident in England and Wales"),

    @JsonProperty("B")
    APPLICANT_RESPONDENT_ONE_RESIDENT("The applicant and respondent were last habitually resident in England and Wales and one "
        + "of them still resides there"),

    @JsonProperty("A")
    APPLICANT_RESPONDENT_RESIDENT("The applicant and the respondent are habitually resident in England and Wales");

    private final String label;
}
