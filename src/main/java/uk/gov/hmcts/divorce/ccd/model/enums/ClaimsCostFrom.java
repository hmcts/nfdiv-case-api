package uk.gov.hmcts.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ClaimsCostFrom implements HasLabel {

    @JsonProperty("co-applicant")
    CO_APPLICANT("Co-applicant"),

    //TODO: If respondent becomes co-applicant, what does a co-respondent become?
    @JsonProperty("correspondent")
    CORRESPONDENT("Co-respondent");

    private final String label;
}
