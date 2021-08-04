package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ServiceMethod implements HasLabel {

    @JsonProperty("solicitorService")
    SOLICITOR_SERVICE("Solicitor Service"),

    @JsonProperty("courtService")
    COURT_SERVICE("Court Service");

    private final String label;
}
