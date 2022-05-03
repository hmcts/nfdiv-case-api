package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ProgressPaperCase implements HasLabel {

    @JsonProperty("awaitingDocuments")
    AWAITING_DOCUMENTS("Awaiting applicant"),

    @JsonProperty("awaitingPayment")
    AWAITING_PAYMENT("Awaiting payment"),

    @JsonProperty("submitted")
    SUBMITTED("Submitted"),

    @JsonProperty("awaitingHwfDecision")
    AWAITING_HWF_DECISION("Awaiting HWF Decision");

    private final String label;
}
