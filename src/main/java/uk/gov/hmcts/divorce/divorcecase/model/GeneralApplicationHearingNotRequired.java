package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralApplicationHearingNotRequired implements HasLabel {
    @JsonProperty("yesPartnerAgreesWithApplication")
    YES_PARTNER_AGREES_WITH_APPLICATION("Yes, because the other party agrees with my application"),

    @JsonProperty("yesPartnerAgreesWithNoHearing")
    YES_PARTNER_AGREES_WITH_NO_HEARING("Yes, because the other party agrees to this being dealt with without a hearing"),

    @JsonProperty("yesDoesNotNeedConsent")
    YES_DOES_NOT_NEED_CONSENT("Yes, because the application does not need consent"),

    @JsonProperty("no")
    NO("No, a hearing is required");

    private final String label;
}
