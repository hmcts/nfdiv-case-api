package uk.gov.hmcts.divorce.common.validation;

import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.Gender;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ValidationUtil {

    public static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    public static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";
    public static final String IN_THE_FUTURE = " can not be in the future.";
    public static final String EMPTY = " cannot be empty or null";
    public static final String MUST_BE_YES = " must be YES";
    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";

    private ValidationUtil() {
    }

    public static void validateBasicCase(CaseData caseData, List<String> errorList) {
        addToErrorList(checkIfStringNullOrEmpty(caseData.getPetitionerFirstName(), "PetitionerFirstName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getPetitionerLastName(), "PetitionerLastName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getRespondentFirstName(), "RespondentFirstName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getRespondentLastName(), "RespondentLastName"), errorList);
        addToErrorList(checkIfYesOrNoNullOrEmpty(caseData.getFinancialOrder(), "FinancialOrder"), errorList);
        addToErrorList(checkIfGenderNullOrEmpty(caseData.getInferredPetitionerGender(), "InferredPetitionerGender"), errorList);
        addToErrorList(checkIfGenderNullOrEmpty(caseData.getInferredRespondentGender(), "InferredRespondentGender"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getMarriagePetitionerName(), "MarriagePetitionerName"), errorList);
        addToErrorList(checkIfConfidentialAddressNullOrEmpty(caseData.getPetitionerContactDetailsConfidential(),
            "PetitionerContactDetailsConfidential"), errorList);
        addToErrorList(checkIfYesOrNoIsNullOrEmptyOrNo(caseData.getPrayerHasBeenGiven(), "PrayerHasBeenGiven"), errorList);
        addToErrorList(checkIfYesOrNoIsNullOrEmptyOrNo(caseData.getStatementOfTruth(), "StatementOfTruth"), errorList);
        addToErrorList(checkIfDateIsAllowed(caseData.getMarriageDate(), "MarriageDate"), errorList);
        addListToErrorList(validateJurisdictionConnection(caseData.getJurisdictionConnections(), caseData), errorList);
    }


    public static void addToErrorList(String error, List<String> errorList) {
        if (error != null) {
            errorList.add(error);
        }
    }

    public static void addListToErrorList(List<String> errors, List<String> errorList) {
        if (errors != null) {
            errorList.addAll(errors);
        }
    }

    public static String checkIfStringNullOrEmpty(String string, String field) {
        if (string == null) {
            return field + EMPTY;
        }
        return null;
    }

    public static String checkIfYesOrNoNullOrEmpty(YesOrNo yesOrNo, String field) {
        if (yesOrNo == null) {
            return field + EMPTY;
        }
        return null;
    }

    public static String checkIfGenderNullOrEmpty(Gender gender, String field) {
        if (gender == null) {
            return field + EMPTY;
        }
        return null;
    }

    public static String checkIfConfidentialAddressNullOrEmpty(ConfidentialAddress confidentialAddress, String field) {
        if (confidentialAddress == null) {
            return field + EMPTY;
        }
        return null;
    }

    public static String checkIfYesOrNoIsNullOrEmptyOrNo(YesOrNo yesOrNo, String field) {
        if (yesOrNo == null) {
            return field + EMPTY;
        } else if (yesOrNo.equals(YesOrNo.NO)) {
            return field + MUST_BE_YES;
        }
        return null;
    }

    public static String checkIfDateIsAllowed(LocalDate localDate, String field) {
        if (localDate == null) {
            return field + EMPTY;
        } else if (isLessThanOneYearAgo(localDate)) {
            return field + LESS_THAN_ONE_YEAR_AGO;
        } else if (isOverOneHundredYearsAgo(localDate)) {
            return field + MORE_THAN_ONE_HUNDRED_YEARS_AGO;
        } else if (isInTheFuture(localDate)) {
            return field + IN_THE_FUTURE;
        }
        return null;
    }

    private static boolean isLessThanOneYearAgo(LocalDate date) {
        return !date.isAfter(LocalDate.now())
            && date.isAfter(LocalDate.now().minus(365, ChronoUnit.DAYS));
    }

    private static boolean isOverOneHundredYearsAgo(LocalDate date) {
        return date.isBefore(LocalDate.now().minus(100L * 365, ChronoUnit.DAYS));
    }

    private static boolean isInTheFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    public static List<String> validateJurisdictionConnection(Set<JurisdictionConnections> jurisdictionConnections, CaseData
        caseData) {
        List<String> errors = new ArrayList<>();

        if (jurisdictionConnections == null) {
            errors.add("JurisdictionConnections" + EMPTY);
            return errors;
        } if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESP_RESIDENT) && caseData.getJurisdictionPetitionerResidence() != YesOrNo.YES && caseData.getJurisdictionRespondentResidence() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.PET_RESP_RESIDENT + CANNOT_EXIST);
        } if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESP_LAST_RESIDENT) && caseData.getJurisdictionBothLastHabituallyResident() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.PET_RESP_LAST_RESIDENT + CANNOT_EXIST);
        } if (jurisdictionConnections.contains(JurisdictionConnections.RESP_RESIDENT) && caseData.getJurisdictionRespondentResidence() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.RESP_RESIDENT + CANNOT_EXIST);
        } if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESIDENT_TWELVE_MONTHS) && caseData.getJurisdictionPetitionerResidence() != YesOrNo.YES && caseData.getJurisdictionPetHabituallyResLastTwelveMonths() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.PET_RESIDENT_TWELVE_MONTHS + CANNOT_EXIST);
        } if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESIDENT_SIX_MONTHS) && caseData.getJurisdictionPetitionerResidence() != YesOrNo.YES && caseData.getJurisdictionPetHabituallyResLastSixMonths() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.PET_RESIDENT_SIX_MONTHS + CANNOT_EXIST);
        } if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESP_DOMICILED) && caseData.getJurisdictionPetitionerDomicile() != YesOrNo.YES && caseData.getJurisdictionRespondentDomicile() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.PET_RESP_DOMICILED + CANNOT_EXIST);
        } if (jurisdictionConnections.contains(JurisdictionConnections.RESIDUAL_JURISDICTION) && caseData.getJurisdictionResidualEligible() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION + CANNOT_EXIST);
        } if (jurisdictionConnections.contains(JurisdictionConnections.PET_DOMICILED) && caseData.getJurisdictionPetitionerDomicile() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.PET_DOMICILED + CANNOT_EXIST);
        } if (jurisdictionConnections.contains(JurisdictionConnections.RESP_DOMICILED) && caseData.getJurisdictionRespondentDomicile() != YesOrNo.YES) {
            errors.add(CONNECTION + JurisdictionConnections.RESP_DOMICILED + CANNOT_EXIST);
        }
        return errors;
    }

}
