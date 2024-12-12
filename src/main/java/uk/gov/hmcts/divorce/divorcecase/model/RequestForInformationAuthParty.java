package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RequestForInformationAuthParty implements HasLabel {
    @JsonProperty("applicant1")
    APPLICANT1("Applicant 1"),

    @JsonProperty("applicant2")
    APPLICANT2("Applicant 2"),

    @JsonProperty("both")
    BOTH("Both"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
