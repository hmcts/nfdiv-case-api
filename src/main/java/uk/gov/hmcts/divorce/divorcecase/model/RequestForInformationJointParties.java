package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RequestForInformationJointParties implements HasLabel {
    @JsonProperty("applicant1")
    APPLICANT1("Applicant 1 / Applicant 1's Solicitor"),

    @JsonProperty("applicant2")
    APPLICANT2("Applicant 2 / Applicant 2's Solicitor"),

    @JsonProperty("both")
    BOTH("Applicant 1 and 2 / Applicant 1 and 2's Solicitors"),

    @JsonProperty("other")
    OTHER("Other - email address");

    private final String label;
}
