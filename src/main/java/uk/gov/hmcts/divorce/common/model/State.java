package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateBasicCase;

@RequiredArgsConstructor
@Getter
public enum State implements CaseState {

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
    AwaitingPayment("AwaitingPayment") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();
            validateBasicCase(caseData, errors);
            return errors;
        }
    },

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
    SolicitorAwaitingPaymentConfirmation("SolicitorAwaitingPaymentConfirmation"),

    @JsonProperty("AwaitingDocuments")
    @CCD(
        label = "Case created and awaiting action by the petitioner",
        name = "Awaiting petitioner"
    )
    AwaitingDocuments("AwaitingDocuments");

    private final String name;

}

