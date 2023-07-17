package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SupplementaryCaseType implements HasLabel {

    @JsonProperty("notApplicable")
    NA("Not Applicable"),

    @JsonProperty("judicialSeparation")
    JUDICIAL_SEPARATION("Judicial Separation"),

    @JsonProperty("separation")
    SEPARATION("Separation");

    private final String label;

    public boolean isJudicialSeparation() {
        return JUDICIAL_SEPARATION.name().equalsIgnoreCase(this.name()) || SEPARATION.name().equalsIgnoreCase(this.name());
    }

    public boolean isNotApplicable() {
        return NA.name().equalsIgnoreCase(this.name());
    }
}
