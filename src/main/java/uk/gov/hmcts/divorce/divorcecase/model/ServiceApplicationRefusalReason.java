package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ServiceApplicationRefusalReason implements HasLabel {

    @JsonProperty("adminRefusal")
    ADMIN_REFUSAL("Admin Refusal"),

    @JsonProperty("refusalOrderToApplicant")
    REFUSAL_ORDER_TO_APPLICANT("Refusal order to applicant");

    private final String label;
}
