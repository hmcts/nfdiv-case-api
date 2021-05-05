package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum PaymentStatus implements HasLabel {

    @JsonProperty("inProgress")
    IN_PROGRESS("In Progress"),

    @JsonProperty("success")
    SUCCESS("Success"),

    @JsonProperty("declined")
    DECLINED("Declined"),

    @JsonProperty("timedOut")
    TIMED_OUT("Timed out"),

    @JsonProperty("cancelled")
    CANCELLED("Cancelled"),

    @JsonProperty("error")
    ERROR("Error");

    private final String label;
}
