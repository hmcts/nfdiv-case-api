package uk.gov.hmcts.divorce.divorcecase.validation;

import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import java.time.LocalDate;
import java.util.List;

import static java.time.temporal.ChronoUnit.YEARS;

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
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant1().getFirstName(), "Applicant1FirstName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant1().getLastName(), "Applicant1LastName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant2().getFirstName(), "Applicant2FirstName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant2().getLastName(), "Applicant2LastName"), errorList);
        addToErrorList(checkIfYesOrNoNullOrEmpty(caseData.getApplicant1().getFinancialOrder(), "Applicant1FinancialOrder"), errorList);
        addToErrorList(checkIfGenderNullOrEmpty(caseData.getApplicant1().getGender(), "Applicant1Gender"), errorList);
        addToErrorList(checkIfGenderNullOrEmpty(caseData.getApplicant2().getGender(), "Applicant2Gender"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplication().getMarriageDetails().getApplicant1Name(),
            "MarriageApplicant1Name"),
            errorList);
        addToErrorList(checkIfConfidentialAddressNullOrEmpty(caseData.getApplicant1().getContactDetailsConfidential(),
            "Applicant1ContactDetailsConfidential"), errorList);
        addToErrorList(checkIfYesOrNoIsNullOrEmptyOrNo(
            caseData.getApplication().getApplicant1PrayerHasBeenGiven(), "Applicant1PrayerHasBeenGiven"), errorList);
        addToErrorList(
            checkIfYesOrNoIsNullOrEmptyOrNo(caseData.getApplication().getApplicant1StatementOfTruth(), "Applicant1StatementOfTruth"),
            errorList);
        addToErrorList(checkIfDateIsAllowed(caseData.getApplication().getMarriageDetails().getDate(), "MarriageDate"), errorList);
        addListToErrorList(caseData.getApplication().getJurisdiction().validate(), errorList);
    }

    public static void validateApplicant1BasicCase(CaseData caseData, List<String> errorList) {
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant1().getFirstName(), "Applicant1FirstName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant1().getLastName(), "Applicant1LastName"), errorList);
        addToErrorList(checkIfYesOrNoNullOrEmpty(caseData.getApplicant1().getFinancialOrder(), "Applicant1FinancialOrder"), errorList);
        addToErrorList(checkIfGenderNullOrEmpty(caseData.getApplicant1().getGender(), "Applicant1Gender"), errorList);
        addToErrorList(checkIfGenderNullOrEmpty(caseData.getApplicant2().getGender(), "Applicant2Gender"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(
            caseData.getApplication().getMarriageDetails().getApplicant1Name(), "MarriageApplicant1Name"), errorList);
        addToErrorList(checkIfDateIsAllowed(caseData.getApplication().getMarriageDetails().getDate(), "MarriageDate"), errorList);
        addListToErrorList(caseData.getApplication().getJurisdiction().validate(), errorList);
    }

    public static void validateApplicant2BasicCase(CaseData caseData, List<String> errorList) {
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant2().getFirstName(), "Applicant2FirstName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant2().getLastName(), "Applicant2LastName"), errorList);
        addToErrorList(checkIfYesOrNoNullOrEmpty(
            caseData.getApplication().getApplicant2StatementOfTruth(), "Applicant2StatementOfTruth"), errorList);
        addToErrorList(checkIfYesOrNoNullOrEmpty(
            caseData.getApplication().getApplicant2PrayerHasBeenGiven(), "Applicant2PrayerHasBeenGiven"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(
            caseData.getApplication().getMarriageDetails().getApplicant2Name(), "MarriageApplicant2Name"), errorList);
        addToErrorList(checkIfDateIsAllowed(caseData.getApplication().getMarriageDetails().getDate(), "MarriageDate"), errorList);
        addListToErrorList(caseData.getApplication().getJurisdiction().validate(), errorList);
    }

    public static void validateApplicant2RequestChanges(CaseData caseData, List<String> errorList) {
        addToErrorList(checkIfYesOrNoNullOrEmpty(caseData.getApplication().getApplicant2ConfirmApplicant1Information(),
            "Applicant2ConfirmApplicant1Information"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplication().getApplicant2ExplainsApplicant1IncorrectInformation(),
            "Applicant2ExplainsApplicant1IncorrectInformation"), errorList);
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
            && date.isAfter(LocalDate.now().minus(1, YEARS));
    }

    private static boolean isOverOneHundredYearsAgo(LocalDate date) {
        return date.isBefore(LocalDate.now().minus(100, YEARS));
    }

    private static boolean isInTheFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    public static void validateCaseFieldsForIssueApplication(MarriageDetails marriageDetails, List<String> errorList) {
        //MarriageApplicant1Name and MarriageDate are validated in validateBasicCase
        addToErrorList(checkIfStringNullOrEmpty(marriageDetails.getApplicant2Name(), "MarriageApplicant2Name"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(marriageDetails.getPlaceOfMarriage(), "PlaceOfMarriage"), errorList);
    }
}
