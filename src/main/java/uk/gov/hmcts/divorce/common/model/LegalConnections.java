package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum LegalConnections implements HasLabel {

    @JsonProperty("A")
    COURTS_RESIDUAL_JURISDICTION("The courts of England and Wales have residual jurisdiction"),

    @JsonProperty("B")
    APP_1_APP_2_DOMICILED("The applicant and the respondent are both domiciled in England and Wales"),

    @JsonProperty("C")
    APP_1_DOMICILED_RESIDENT("The applicant is domiciled and habitually resident in England and Wales and has resided "
        + "there for at least six months immediately prior to the application"),

    @JsonProperty("D")
    APP_1_RESIDENT("The applicant is habitually resident in England and Wales and has resided there for at least a year "
        + "immediately prior to the presentation of the application"),

    @JsonProperty("E")
    APP_2_RESIDENT("The respondent is habitually resident in England and Wales"),

    @JsonProperty("F")
    APP_1_APP_2_ONE_RESIDENT("The applicant and the respondent were last habitually resident in England and Wales and one "
        + "of them still resides there"),

    @JsonProperty("G")
    APP_1_APP_2_RESIDENT("The applicant and the respondent are habitually resident in England and Wales");

    private final String label;
}
