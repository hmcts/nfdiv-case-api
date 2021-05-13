package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.hasAwaitingDocuments;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.isPaymentIncomplete;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateBasicCase;

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
        label = "Application paid and submitted",
        name = "Application submitted"
    )
    Submitted("Submitted") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();

            if (isPaymentIncomplete(caseData)) {
                errors.add("Payment incomplete");
            }
            if (hasAwaitingDocuments(caseData)) {
                errors.add("Awaiting documents");
            }

            return errors;
        }
    },

    @JsonProperty("SolicitorAwaitingPaymentConfirmation")
    @CCD(
        label = "Solicitor - Awaiting Payment Confirmation",
        name = "Solicitor - Awaiting Payment Confirmation"
    )
    SolicitorAwaitingPaymentConfirmation("SolicitorAwaitingPaymentConfirmation"),

    @JsonProperty("AwaitingDocuments")
    @CCD(
        label = "Case created and awaiting action by applicant 1",
        name = "Awaiting applicant 1"
    )
    AwaitingDocuments("AwaitingDocuments") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();

            if (isPaymentIncomplete(caseData)) {
                errors.add("Payment incomplete");
            }
            if (!hasAwaitingDocuments(caseData)) {
                errors.add("No Awaiting documents");
            }

            return errors;
        }
    };

    private final String name;

    public List<String> validate(CaseData data) {
        return null;
    }

}

