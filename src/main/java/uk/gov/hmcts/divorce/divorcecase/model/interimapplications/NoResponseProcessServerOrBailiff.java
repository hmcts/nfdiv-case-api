package uk.gov.hmcts.divorce.divorcecase.model.interimapplications;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponseProcessServerOrBailiff implements HasLabel {
    @JsonProperty("processServer")
    PROCESS_SERVER("I want to arrange for service by a process server"),

    @JsonProperty("courtBailiff")
    COURT_BAILIFF("I want to request bailiff service");

    private final String label;
}
