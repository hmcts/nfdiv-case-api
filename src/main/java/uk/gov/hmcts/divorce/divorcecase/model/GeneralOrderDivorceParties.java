package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralOrderDivorceParties implements HasLabel {
    @JsonProperty("petitioner")
    PETITIONER("Petitioner"),

    @JsonProperty("respondent")
    RESPONDENT("Respondent"),

    @JsonProperty("coRespondent")
    CO_RESPONDENT("Co-respondent");

    private final String label;
}
