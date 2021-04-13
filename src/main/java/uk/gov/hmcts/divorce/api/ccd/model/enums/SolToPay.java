package uk.gov.hmcts.divorce.api.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SolToPay implements HasLabel {

    @JsonProperty("feePayByAccount")
    FEE_PAY_BY_ACCOUNT("Fee account"),

    @JsonProperty("feesHelpWith")
    FEES_HELP_WITH("Help with fees");

    private final String label;
}
