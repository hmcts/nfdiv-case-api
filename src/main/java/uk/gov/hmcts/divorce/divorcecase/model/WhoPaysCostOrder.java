package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum WhoPaysCostOrder implements HasLabel {

    @JsonProperty("respondentAndCoRespondent")
    RESPONDENT_AND_CO_RESPONDENT("Respondent and co-respondent"),

    @JsonProperty("coRespondent")
    CO_RESPONDENT("Co-respondent"),

    @JsonProperty("respondent")
    RESPONDENT("Respondent");

    private final String label;
}
