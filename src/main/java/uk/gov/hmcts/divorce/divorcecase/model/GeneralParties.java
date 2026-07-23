package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralParties implements HasLabel {
    @JsonProperty("applicant")
    APPLICANT("Applicant / Applicant's Solicitor"),

    @JsonProperty("applicant2")
    APPLICANT2("Applicant 2 / Applicant 2's Solicitor"),

    @JsonProperty("respondent")
    RESPONDENT("Respondent / Respondent's Solicitor"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;

    public static GeneralParties from(boolean isApplicant1, ApplicationType applicationType) {
        if (isApplicant1) {
            return GeneralParties.APPLICANT;
        } else {
            return ApplicationType.SOLE_APPLICATION.equals(applicationType) ? GeneralParties.RESPONDENT : GeneralParties.APPLICANT2;
        }
    }
}
