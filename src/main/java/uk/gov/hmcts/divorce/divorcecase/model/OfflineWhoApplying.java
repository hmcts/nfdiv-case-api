package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OfflineWhoApplying {

    @JsonProperty("applicant1")
    APPLICANT_1("Applicant 1"),

    @JsonProperty("applicant2")
    APPLICANT_2("Applicant 2");

    private final String label;
}
