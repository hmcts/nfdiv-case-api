package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum HowToRespondApplication implements HasLabel {

    @JsonProperty("withoutDisputeDivorce")
    WITHOUT_DISPUTE_DIVORCE("withoutDisputeDivorce", "Continue without disputing the divorce"),

    @JsonProperty("disputeDivorce")
    DISPUTE_DIVORCE("disputeDivorce", "The applicant wants to dispute the divorce");

    private String type;
    private String label;
}
