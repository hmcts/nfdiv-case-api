package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;


@Getter
@RequiredArgsConstructor
public enum OnlinePaymentMethod implements HasLabel {
    CARD("card"),
    PAYMENT_BY_ACCOUNT("payment by account");

    @JsonValue
    private final String label;
}
