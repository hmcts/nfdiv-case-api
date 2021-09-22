package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum FinancialOrderFor implements HasLabel {

    @JsonProperty("applicant")
    @JsonAlias({"applicant1"})
    APPLICANT("The applicant"),

    @JsonProperty("children")
    CHILDREN("The children");

    private final String label;
}
