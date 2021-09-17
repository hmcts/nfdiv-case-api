package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentsServedBeingThe implements HasLabel {

    @JsonProperty("litigationFriend")
    LITIGATION_FRIEND("Litigation friend"),

    @JsonProperty("solicitors")
    SOLICITOR("Solicitor's"),

    @JsonProperty("respondents")
    RESPONDENT("Respondent's"),

    @JsonProperty("applicants")
    APPLICANT("Applicant's");

    private final String label;
}
