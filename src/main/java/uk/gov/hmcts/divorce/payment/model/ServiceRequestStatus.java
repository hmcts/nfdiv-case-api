package uk.gov.hmcts.divorce.payment.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;


@Getter
@RequiredArgsConstructor
public enum ServiceRequestStatus implements HasLabel {
    PAID("Paid"),
    PARTIALLY_PAID("Partially paid"),
    NOT_PAID("Not paid");

    private final String label;
}
