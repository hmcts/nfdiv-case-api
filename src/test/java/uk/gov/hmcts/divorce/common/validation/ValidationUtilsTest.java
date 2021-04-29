package uk.gov.hmcts.divorce.common.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ValidationUtilsTest {

    private static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    private static final String EMPTY = " cannot be empty or null";
    private static final String MUST_BE_YES = " must be YES";
    private static final String IN_THE_FUTURE = " can not be in the future.";
    private static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";

    @Test
    public void shouldAddValidErrorToList() {
        List<String> errors = new ArrayList<>();
        String errorMessage = "Some error";

        ValidationUtils.addToErrorList(errorMessage, errors);
        assertThat(errors.get(0), is(errorMessage));
    }

    @Test
    public void shouldNotAddInvalidErrorToList() {
        List<String> errors = new ArrayList<>();

        ValidationUtils.addToErrorList(null, errors);
        assertThat(errors.size(), is(0));
    }

    @Test
    public void shouldReturnErrorWhenStringIsNull() {
        String response = ValidationUtils.checkIfStringNullOrEmpty(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenYesOrNoIsNull() {
        String response = ValidationUtils.checkIfYesOrNoNullOrEmpty(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenGenderIsNull() {
        String response = ValidationUtils.checkIfGenderNullOrEmpty(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenConfidentialAddressIsNull() {
        String response = ValidationUtils.checkIfConfidentialAddressNullOrEmpty(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenYesOrNoIsNo() {
        String response = ValidationUtils.checkIfYesOrNoIsNullOrEmptyOrNo(YesOrNo.NO, "field");

        assertThat(response, is("field" + MUST_BE_YES));
    }

    @Test
    public void shouldReturnErrorWhenYesOrNoIsInvalid() {
        String response = ValidationUtils.checkIfYesOrNoIsNullOrEmptyOrNo(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenDateIsInTheFuture() {
        String response = ValidationUtils.checkIfDateIsAllowed(LocalDate.now().plus(2, ChronoUnit.YEARS), "field");

        assertThat(response, is("field" + IN_THE_FUTURE));
    }

    @Test
    public void shouldReturnErrorWhenDateIsOverOneHundredYearsAgo() {
        String response = ValidationUtils.checkIfDateIsAllowed(LocalDate.now().minus(365 * 100 + 1, ChronoUnit.DAYS), "field");

        assertThat(response, is("field" + MORE_THAN_ONE_HUNDRED_YEARS_AGO));
    }

    @Test
    public void shouldReturnErrorWhenDateIsLessThanOneYearAgo() {
        String response = ValidationUtils.checkIfDateIsAllowed(LocalDate.now().minus(360, ChronoUnit.DAYS), "field");

        assertThat(response, is("field" + LESS_THAN_ONE_YEAR_AGO));
    }
}
