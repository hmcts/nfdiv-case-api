package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
            errors.add(checkIfNullOrEmpty(data.getPetitionerFirstName(), "PetitionerFirstName"));
            errors.add(checkIfNullOrEmpty(data.getPetitionerLastName(), "PetitionerLastName"));
            errors.add(checkIfNullOrEmpty(data.getRespondentFirstName(), "RespondentFirstName"));
            errors.add(checkIfNullOrEmpty(data.getRespondentLastName(), "RespondentLastName"));
//            errors.add(checkIfNullOrEmpty(data.getFinancialOrder().toString(), "FinancialOrder"));
//            errors.add(checkIfNullOrEmpty(data.getInferredPetitionerGender().toString(), "InferredPetitionerGender"));
//            errors.add(checkIfNullOrEmpty(data.getInferredRespondentGender().toString(), "InferredRespondentGender"));
            errors.add(checkIfNullOrEmpty(data.getMarriagePetitionerName(), "MarriagePetitionerName"));
//            errors.add(checkIfNullOrEmpty(data.getPetitionerContactDetailsConfidential().toString(), "PetitionerContactDetailsConfidential"));
//            errors.add(checkIfNullOrEmptyOrNo(data.getPrayerHasBeenGiven().toString(), "PrayerHasBeenGiven"));
//            errors.add(checkIfNullOrEmptyOrNo(data.getStatementOfTruth().toString(), "StatementOfTruth"));
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

    protected String checkIfNullOrEmpty(String string, String field) {
        String EMPTY = " cannot be empty or null";
        if (Optional.ofNullable(string).isEmpty()) {
            return field + EMPTY;
        }
        return null;
    }

    protected String checkIfNullOrEmptyOrNo(String string, String field) {
        String EMPTY = " cannot be empty or null";
        String YES = " must be YES";
        if (Optional.ofNullable(string).isEmpty()) {
            return field + EMPTY;
        } else if (string.equals(YesOrNo.NO.toString())) {
            return field + YES;
        }
        return null;
    }

    protected String checkIfSetNullOrEmpty(Set set, String field) {
        String EMPTY = " cannot be empty";
        if (set.isEmpty()) {
            return field + EMPTY;
        }
        return null;
    }

    protected String checkIfDateIsAllowed(LocalDate localDate, String field) {
        String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
        String MORE_THAN_ONE_HUNDRED_YEARS_AGO = "MarriageDate can not be more than 100 years ago.";
        String IN_THE_FUTURE = "MarriageDate can not be in the future.";
        if (isLessThanOneYearAgo(localDate)) {
            return field + LESS_THAN_ONE_YEAR_AGO;
        } else if (isOverOneHundredYearsAgo(localDate)) {
            return field + MORE_THAN_ONE_HUNDRED_YEARS_AGO;
        } else if (isInTheFuture(localDate)) {
            return field + IN_THE_FUTURE;
        } else if (localDate.toString().isEmpty()) {
            checkIfNullOrEmpty(localDate.toString(), field);
        }
        return null;
    }

    private boolean isLessThanOneYearAgo(LocalDate date) {
        return !date.isAfter(LocalDate.now())
            && date.isAfter(LocalDate.now().minus(365, ChronoUnit.DAYS));
    }

    private boolean isOverOneHundredYearsAgo(LocalDate date) {
        return date.isBefore(LocalDate.now().minus(365 * 100, ChronoUnit.DAYS));
    }

    private boolean isInTheFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

}

