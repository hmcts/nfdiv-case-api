package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum WithdrawApplicationReasonType implements HasLabel {

    @JsonProperty("refundProcessed")
    REFUND_PROCESSED("Refund Processed"),

    @JsonProperty("refundNotRequired")
    REFUND_NOT_REQUIRED("Refund Not Required"),

    @JsonProperty("orderFromJudge")
    ORDER_FROM_JUDGE("Order from Judge"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
