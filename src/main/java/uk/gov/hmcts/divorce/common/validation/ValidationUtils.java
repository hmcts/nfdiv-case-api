package uk.gov.hmcts.divorce.common.validation;

import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.Gender;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ValidationUtils {

    public void addToErrorList(String error, List<String> errorList) {
        if (error != null) {
            errorList.add(error);
        }
    }

    public String checkIfStringNullOrEmpty(String string, String field) {
        String EMPTY = " cannot be empty or null";
        if (string == null) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfYesOrNoNullOrEmpty(YesOrNo yesOrNo, String field) {
        String EMPTY = " cannot be empty or null";
        if (yesOrNo == null) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfGenderNullOrEmpty(Gender gender, String field) {
        String EMPTY = " cannot be empty or null";
        if (gender == null) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfConfidentialAddressNullOrEmpty(ConfidentialAddress confidentialAddress, String field) {
        String EMPTY = " cannot be empty or null";
        if (confidentialAddress == null) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfYesOrNoIsNullOrEmptyOrNo(YesOrNo yesOrNo, String field) {
        String EMPTY = " cannot be empty or null";
        String MUST_BE_YES = " must be YES";
        if (yesOrNo == null) {
            return field + EMPTY;
        } else if (yesOrNo.equals(YesOrNo.NO)) {
            return field + MUST_BE_YES;
        }
        return null;
    }

    public String checkIfSetNullOrEmpty(Set set, String field) {
        String EMPTY = " cannot be empty";
        if (set.isEmpty()) {
            return field + EMPTY;
        }
        return null;
    }

    public String checkIfDateIsAllowed(LocalDate localDate, String field) {
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
