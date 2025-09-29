package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;


@Getter
@RequiredArgsConstructor
public enum ServiceRequestStatus implements HasLabel {
    PAID("Paid"),
    PARTIALLY_PAID("Partially paid"),
    NOT_PAID("Not paid");

    @JsonValue
    private final String label;
}
