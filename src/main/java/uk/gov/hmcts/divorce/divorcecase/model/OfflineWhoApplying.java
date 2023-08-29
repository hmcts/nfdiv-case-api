package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum OfflineWhoApplying implements HasLabel {

    @JsonProperty("applicant1")
    APPLICANT_1("Applicant 1"),

    @JsonProperty("applicant2")
    APPLICANT_2("Applicant 2 / Respondent");

    private final String label;
}
