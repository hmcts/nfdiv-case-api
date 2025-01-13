package uk.gov.hmcts.divorce.payment.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;


@Getter
@RequiredArgsConstructor
public enum OnlinePaymentMethod implements HasLabel {
    CARD("card"),
    PAYMENT_BY_ACCOUNT("payment by account");

    private final String label;
}
