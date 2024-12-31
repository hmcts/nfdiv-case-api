package uk.gov.hmcts.divorce.payment.model.callback;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;


@Getter
@AllArgsConstructor
public enum OnlinePaymentMethod implements HasLabel {

    @JsonProperty("card")
    CARD("Card payment"),

    @JsonProperty("payment by account")
    PAYMENT_BY_ACCOUNT("Payment by account");

    private final String label;
}
