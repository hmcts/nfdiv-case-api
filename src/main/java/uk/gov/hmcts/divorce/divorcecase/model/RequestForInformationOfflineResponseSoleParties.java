package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RequestForInformationOfflineResponseSoleParties implements HasLabel {
    @JsonProperty("applicant")
    APPLICANT("Applicant"),

    @JsonProperty("applicantSolicitor")
    APPLICANTSOLICITOR("Applicant's Solicitor"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
