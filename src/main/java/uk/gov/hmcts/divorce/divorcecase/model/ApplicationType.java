package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ApplicationType implements HasLabel {

    @JsonProperty("soleApplication")
    SOLE_APPLICATION("Sole Application"),

    @JsonProperty("jointApplication")
    JOINT_APPLICATION("Joint Application");

    private final String label;

    public boolean isSole() {
        return SOLE_APPLICATION.name().equalsIgnoreCase(this.name());
    }
}
