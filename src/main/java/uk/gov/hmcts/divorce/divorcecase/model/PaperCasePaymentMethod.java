package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum PaperCasePaymentMethod implements HasLabel {

    @JsonProperty("phone")
    PHONE("Phone"),

    @JsonProperty("chequeOrPostalOrder")
    CHEQUE_OR_POSTAL_ORDER("Cheque/Postal Order");

    private final String label;
}
