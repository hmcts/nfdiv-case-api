package uk.gov.hmcts.reform.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SolServiceMethod implements HasLabel {

    @JsonProperty("personalService")
    PERSONAL_SERVICE("Personal Service"),

    @JsonProperty("courtService")
    COURT_SERVICE("Court Service");

    private final String label;
}
