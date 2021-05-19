package uk.gov.hmcts.divorce.common.validation;

import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.Gender;

import java.time.LocalDate;
import java.util.List;

import static java.time.temporal.ChronoUnit.YEARS;
import static org.springframework.util.CollectionUtils.isEmpty;

public final class ValidationUtil {

    public static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    public static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";
    public static final String IN_THE_FUTURE = " can not be in the future.";
    public static final String EMPTY = " cannot be empty or null";
    public static final String MUST_BE_YES = " must be YES";
    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";
    private static final int FEE_PENCE = 55000; // TODO get from order summary

    private ValidationUtil() {
    }

    public static void validateBasicCase(CaseData caseData, List<String> errorList) {
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant1FirstName(), "Applicant1FirstName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant1LastName(), "Applicant1LastName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant2FirstName(), "Applicant2FirstName"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getApplicant2LastName(), "Applicant2LastName"), errorList);
        addToErrorList(checkIfYesOrNoNullOrEmpty(caseData.getFinancialOrder(), "FinancialOrder"), errorList);
        addToErrorList(checkIfGenderNullOrEmpty(caseData.getInferredApplicant1Gender(), "InferredApplicant1Gender"), errorList);
        addToErrorList(checkIfGenderNullOrEmpty(caseData.getInferredApplicant2Gender(), "InferredApplicant2Gender"), errorList);
        addToErrorList(checkIfStringNullOrEmpty(caseData.getMarriageApplicant1Name(), "MarriageApplicant1Name"), errorList);
        addToErrorList(checkIfConfidentialAddressNullOrEmpty(caseData.getApplicant1ContactDetailsConfidential(),
            "Applicant1ContactDetailsConfidential"), errorList);
        addToErrorList(checkIfYesOrNoIsNullOrEmptyOrNo(caseData.getPrayerHasBeenGiven(), "PrayerHasBeenGiven"), errorList);
        addToErrorList(checkIfYesOrNoIsNullOrEmptyOrNo(caseData.getStatementOfTruth(), "StatementOfTruth"), errorList);
        addToErrorList(checkIfDateIsAllowed(caseData.getMarriageDetails().getDate(), "MarriageDate"), errorList);
        addListToErrorList(caseData.getJurisdiction().validate(), errorList);
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

    public static boolean isPaymentIncomplete(CaseData caseData) {
        return caseData.getPaymentTotal() < FEE_PENCE;
    }

    public static boolean hasAwaitingDocuments(CaseData caseData) {
        return caseData.getApplicant1WantsToHavePapersServedAnotherWay() == YesOrNo.YES
            || !isEmpty(caseData.getCannotUploadSupportingDocument());
    }

}
