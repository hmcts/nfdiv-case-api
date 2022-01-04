package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SolicitorPaymentMethod implements HasLabel {

    @JsonProperty("feePayByAccount")
    FEE_PAY_BY_ACCOUNT("Solicitor fee account (PBA)"),

    @JsonProperty("feesHelpWith")
    FEES_HELP_WITH("Help With Fees");

    private final String label;
}
