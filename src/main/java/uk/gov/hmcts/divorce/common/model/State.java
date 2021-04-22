package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
public enum State {

    @JsonProperty("Draft")
    @CCD(
        label = "Draft",
        name = "Draft"
    )
    Draft("Draft"),

    @JsonProperty("AwaitingPayment")
    @CCD(
        label = "Awaiting Payment",
        name = "Awaiting Payment"
    )
    AwaitingPayment("AwaitingPayment"),

    @JsonProperty("SOTAgreementPayAndSubmitRequired")
    @CCD(
        label = "Statement of Truth, Pay and Submit Required",
        name = "Statement of Truth, Pay and Submit Required"
    )
    SOTAgreementPayAndSubmitRequired("SOTAgreementPayAndSubmitRequired"),

    @JsonProperty("Submitted")
    @CCD(
        label = "Petition paid and submitted",
        name = "Petition submitted"
    )
    Submitted("Submitted"),

    @JsonProperty("SolicitorAwaitingPaymentConfirmation")
    @CCD(
        label = "Solicitor - Awaiting Payment Confirmation",
        name = "Solicitor - Awaiting Payment Confirmation"
    )
    SolicitorAwaitingPaymentConfirmation("SolicitorAwaitingPaymentConfirmation");

    private final String name;

}

