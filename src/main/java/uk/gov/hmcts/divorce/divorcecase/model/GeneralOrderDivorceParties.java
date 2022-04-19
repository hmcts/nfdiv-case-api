package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralOrderDivorceParties implements HasLabel {
    @JsonProperty("applicant")
    APPLICANT("Applicant"),

    @JsonProperty("respondent")
    RESPONDENT("Respondent / Applicant 2");

    private final String label;
}
