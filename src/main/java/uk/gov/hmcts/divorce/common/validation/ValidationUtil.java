package uk.gov.hmcts.divorce.common.validation;

import lombok.experimental.UtilityClass;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.Gender;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@UtilityClass
public class ValidationUtil {

    public static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    public static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";
    public static final String IN_THE_FUTURE = " can not be in the future.";
    public static final String EMPTY = " cannot be empty or null";
    public static final String MUST_BE_YES = " must be YES";

    public static void addToErrorList(String error, List<String> errorList) {
        if (error != null) {
            errorList.add(error);
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
        return date.isBefore(LocalDate.now().minus(100 * 365, ChronoUnit.DAYS));
    }

    private static boolean isInTheFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

}
