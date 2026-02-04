package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RequestForInformationSoleParties implements HasLabel {
    @JsonProperty("applicant")
    APPLICANT("Applicant / Applicant's Solicitor"),

    @JsonProperty("other")
    OTHER("Other - email address");

    private final String label;
}
