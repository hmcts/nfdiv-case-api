package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.common.validation.ValidationUtils;

import java.util.ArrayList;
import java.util.List;

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
            ValidationUtils validationUtils = new ValidationUtils();
            List<String> errors = new ArrayList<>();
            validationUtils.addToErrorList(validationUtils.checkIfStringNullOrEmpty(data.getPetitionerFirstName(), "PetitionerFirstName"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfStringNullOrEmpty(data.getPetitionerLastName(), "PetitionerLastName"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfStringNullOrEmpty(data.getRespondentFirstName(), "RespondentFirstName"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfStringNullOrEmpty(data.getRespondentLastName(), "RespondentLastName"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfYesOrNoNullOrEmpty(data.getFinancialOrder(), "FinancialOrder"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfGenderNullOrEmpty(data.getInferredPetitionerGender(), "InferredPetitionerGender"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfGenderNullOrEmpty(data.getInferredRespondentGender(), "InferredRespondentGender"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfStringNullOrEmpty(data.getMarriagePetitionerName(), "MarriagePetitionerName"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfConfidentialAddressNullOrEmpty(data.getPetitionerContactDetailsConfidential(), "PetitionerContactDetailsConfidential"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfYesOrNoIsNullOrEmptyOrNo(data.getPrayerHasBeenGiven(), "PrayerHasBeenGiven"), errors);
            validationUtils.addToErrorList(validationUtils.checkIfYesOrNoIsNullOrEmptyOrNo(data.getStatementOfTruth(), "StatementOfTruth"), errors);
//            errors.add(checkIfSetNullOrEmpty(data.getJurisdictionConnections(), "JurisdictionConnections"));
//            errors.add(checkIfDateIsAllowed(data.getMarriageDate(), "MarriageDate"));
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

