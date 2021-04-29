package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.common.validation.ValidationUtils.addToErrorList;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtils.checkIfConfidentialAddressNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtils.checkIfDateIsAllowed;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtils.checkIfGenderNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtils.checkIfStringNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtils.checkIfYesOrNoIsNullOrEmptyOrNo;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtils.checkIfYesOrNoNullOrEmpty;

@RequiredArgsConstructor
@Getter
public enum State implements CaseState {

    @JsonProperty("Draft")
    @CCD(
        label = "Draft",
        name = "Draft"
    )
    Draft("Draft") {
        @Override
        public List<String> validate(CaseData data) {
            return null;
        }
    },

    @JsonProperty("AwaitingPayment")
    @CCD(
        label = "Awaiting Payment",
        name = "Awaiting Payment"
    )
    AwaitingPayment("AwaitingPayment") {
        @Override
        public List<String> validate(CaseData data) {
            List<String> errors = new ArrayList<>();
            addToErrorList(checkIfStringNullOrEmpty(data.getPetitionerFirstName(), "PetitionerFirstName"), errors);
            addToErrorList(checkIfStringNullOrEmpty(data.getPetitionerLastName(), "PetitionerLastName"), errors);
            addToErrorList(checkIfStringNullOrEmpty(data.getRespondentFirstName(), "RespondentFirstName"), errors);
            addToErrorList(checkIfStringNullOrEmpty(data.getRespondentLastName(), "RespondentLastName"), errors);
            addToErrorList(checkIfYesOrNoNullOrEmpty(data.getFinancialOrder(), "FinancialOrder"), errors);
            addToErrorList(checkIfGenderNullOrEmpty(data.getInferredPetitionerGender(), "InferredPetitionerGender"), errors);
            addToErrorList(checkIfGenderNullOrEmpty(data.getInferredRespondentGender(), "InferredRespondentGender"), errors);
            addToErrorList(checkIfStringNullOrEmpty(data.getMarriagePetitionerName(), "MarriagePetitionerName"), errors);
            addToErrorList(checkIfConfidentialAddressNullOrEmpty(data.getPetitionerContactDetailsConfidential(),
                "PetitionerContactDetailsConfidential"), errors);
            addToErrorList(checkIfYesOrNoIsNullOrEmptyOrNo(data.getPrayerHasBeenGiven(), "PrayerHasBeenGiven"), errors);
            addToErrorList(checkIfYesOrNoIsNullOrEmptyOrNo(data.getStatementOfTruth(), "StatementOfTruth"), errors);
            addToErrorList(checkIfDateIsAllowed(data.getMarriageDate(), "MarriageDate"), errors);
            return errors;
        }
    },

    @JsonProperty("SOTAgreementPayAndSubmitRequired")
    @CCD(
        label = "Statement of Truth, Pay and Submit Required",
        name = "Statement of Truth, Pay and Submit Required"
    )
    SOTAgreementPayAndSubmitRequired("SOTAgreementPayAndSubmitRequired") {
        @Override
        public List<String> validate(CaseData data) {
            return null;
        }
    },

    @JsonProperty("Submitted")
    @CCD(
        label = "Petition paid and submitted",
        name = "Petition submitted"
    )
    Submitted("Submitted") {
        @Override
        public List<String> validate(CaseData data) {
            return null;
        }
    },

    @JsonProperty("SolicitorAwaitingPaymentConfirmation")
    @CCD(
        label = "Solicitor - Awaiting Payment Confirmation",
        name = "Solicitor - Awaiting Payment Confirmation"
    )
    SolicitorAwaitingPaymentConfirmation("SolicitorAwaitingPaymentConfirmation") {
        @Override
        public List<String> validate(CaseData data) {
            return null;
        }
    };

    private final String name;

}

