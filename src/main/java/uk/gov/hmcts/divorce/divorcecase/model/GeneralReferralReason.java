package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralReferralReason implements HasLabel {
    @JsonProperty("caseworkerReferral")
    CASEWORKER_REFERRAL("Caseworker referral"),

    @JsonProperty("generalApplicationReferral")
    GENERAL_APPLICATION_REFERRAL("General application referral");

    private final String label;
}
