package uk.gov.hmcts.divorce.common.validation;

import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.Gender;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

public class ValidationUtils {

    public static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    public static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = "MarriageDate can not be more than 100 years ago.";
    public static final String IN_THE_FUTURE = "MarriageDate can not be in the future.";
    public static final String EMPTY = " cannot be empty or null";
    public static final String MUST_BE_YES = " must be YES";

    public void addToErrorList(String error, List<String> errorList) {
        if (error != null) {
            errorList.add(error);
        }
    }

    public String checkIfStringNullOrEmpty(String string, String field) {
        if (string == null) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfYesOrNoNullOrEmpty(YesOrNo yesOrNo, String field) {
        if (yesOrNo == null) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfGenderNullOrEmpty(Gender gender, String field) {
        if (gender == null) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfConfidentialAddressNullOrEmpty(ConfidentialAddress confidentialAddress, String field) {
        if (confidentialAddress == null) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfYesOrNoIsNullOrEmptyOrNo(YesOrNo yesOrNo, String field) {
        if (yesOrNo == null) {
            return field + EMPTY;
        } else if (yesOrNo.equals(YesOrNo.NO)) {
            return field + MUST_BE_YES;
        }
        return null;
    }

    public String checkIfSetNullOrEmpty(Set set, String field) {
        if (set.isEmpty()) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfDateIsAllowed(LocalDate localDate, String field) {
        if (localDate == null) {
            return field + EMPTY;
        } else if (isLessThanOneYearAgo(localDate)) {
            return field + LESS_THAN_ONE_YEAR_AGO;
        } else if (isOverOneHundredYearsAgo(localDate)) {
            return field + MORE_THAN_ONE_HUNDRED_YEARS_AGO;
        } else if (isInTheFuture(localDate)) {
            return field + IN_THE_FUTURE;
        } else if (localDate.toString().isEmpty()) {
            checkIfStringNullOrEmpty(localDate.toString(), field);
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
