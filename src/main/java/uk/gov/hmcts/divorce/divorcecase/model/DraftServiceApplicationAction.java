package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DraftServiceApplicationAction implements HasLabel {

    @JsonProperty("amend")
    AMEND("Amend the existing draft application"),

    @JsonProperty("withdraw")
    WITHDRAW("Withdraw");

    private final String label;

    public boolean isAmend() {
        return this == AMEND;
    }

    public boolean isWithdraw() {
        return this == WITHDRAW;
    }
}
