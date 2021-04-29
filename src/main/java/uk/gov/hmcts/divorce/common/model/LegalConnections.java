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
    PET_RESP_DOMICILED("The Petitioner and Respondent are both domiciled in England and Wales"),

    @JsonProperty("C")
    PET_DOMICILED_RESIDENT("The Petitioner is domiciled and habitually resident in England and Wales and has resided "
        + "there for at least six months immediately prior to the petition"),

    @JsonProperty("D")
    PET_RESIDENT("The Petitioner is habitually resident in England and Wales and has resided there for at least a year "
        + "immediately prior to the presentation of the petition"),

    @JsonProperty("E")
    RESP_RESIDENT("The Respondent is habitually resident in England and Wales"),

    @JsonProperty("F")
    PET_RESP_ONE_RESIDENT("The Petitioner and Respondent were last habitually resident in England and Wales and one "
        + "of them still resides there"),

    @JsonProperty("G")
    PET_RESP_RESIDENT("The Petitioner and the Respondent are habitually resident in England and Wales");

    private final String label;
}
