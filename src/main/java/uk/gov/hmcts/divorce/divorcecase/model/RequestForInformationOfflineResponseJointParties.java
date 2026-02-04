package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RequestForInformationOfflineResponseJointParties implements HasLabel {
    @JsonProperty("applicant1")
    APPLICANT1("Applicant 1"),

    @JsonProperty("applicant1Solicitor")
    APPLICANT1SOLICITOR("Applicant 1's Solicitor"),

    @JsonProperty("applicant2")
    APPLICANT2("Applicant 2"),

    @JsonProperty("applicant2Solicitor")
    APPLICANT2SOLICITOR("Applicant 2's Solicitor"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
