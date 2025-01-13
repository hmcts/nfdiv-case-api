package uk.gov.hmcts.divorce.payment.model.callback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;


@Getter
@AllArgsConstructor
public enum OnlinePaymentMethod implements HasLabel {
    CARD("card"),
    PAYMENT_BY_ACCOUNT("payment by account");

    private final String label;
}
