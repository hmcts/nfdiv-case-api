package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ClaimsCostFrom implements HasLabel {

    @JsonProperty("respondent")
    RESPONDENT("Respondent"),

    @JsonProperty("correspondent")
    CORRESPONDENT("Co-respondent");

    private final String label;
}
