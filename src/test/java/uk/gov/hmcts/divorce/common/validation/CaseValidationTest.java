package uk.gov.hmcts.divorce.common.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.addToErrorList;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfConfidentialAddressNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfDateIsAllowed;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfGenderNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfStringNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfYesOrNoIsNullOrEmptyOrNo;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfYesOrNoNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateBasicCase;

public class CaseValidationTest {

    private static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    private static final String EMPTY = " cannot be empty or null";
    private static final String MUST_BE_YES = " must be YES";
    private static final String IN_THE_FUTURE = " can not be in the future.";
    private static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";

    @Test
    public void shouldValidateBasicCase() {
        CaseData caseData = new CaseData();
        List<String> errors = new ArrayList<>();

        validateBasicCase(caseData, errors);
        assertThat(errors.size(), is(12));
    }

    @Test
    public void shouldAddValidErrorToList() {
        List<String> errors = new ArrayList<>();
        String errorMessage = "Some error";

        addToErrorList(errorMessage, errors);
        assertThat(errors.get(0), is(errorMessage));
    }

    @Test
    public void shouldNotAddInvalidErrorToList() {
        List<String> errors = new ArrayList<>();

        addToErrorList(null, errors);
        assertThat(errors.size(), is(0));
    }

    @Test
    public void shouldReturnErrorWhenStringIsNull() {
        String response = checkIfStringNullOrEmpty(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenYesOrNoIsNull() {
        String response = checkIfYesOrNoNullOrEmpty(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenGenderIsNull() {
        String response = checkIfGenderNullOrEmpty(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenConfidentialAddressIsNull() {
        String response = checkIfConfidentialAddressNullOrEmpty(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenYesOrNoIsNo() {
        String response = checkIfYesOrNoIsNullOrEmptyOrNo(YesOrNo.NO, "field");

        assertThat(response, is("field" + MUST_BE_YES));
    }

    @Test
    public void shouldReturnErrorWhenYesOrNoIsInvalid() {
        String response = checkIfYesOrNoIsNullOrEmptyOrNo(null, "field");

        assertThat(response, is("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenDateIsInTheFuture() {
        String response = checkIfDateIsAllowed(LocalDate.now().plus(2, ChronoUnit.YEARS), "field");

        assertThat(response, is("field" + IN_THE_FUTURE));
    }

    @Test
    public void shouldReturnErrorWhenDateIsOverOneHundredYearsAgo() {
        String response = checkIfDateIsAllowed(LocalDate.now().minus(365 * 100 + 1, ChronoUnit.DAYS), "field");

        assertThat(response, is("field" + MORE_THAN_ONE_HUNDRED_YEARS_AGO));
    }

    @Test
    public void shouldReturnErrorWhenDateIsLessThanOneYearAgo() {
        String response = checkIfDateIsAllowed(LocalDate.now().minus(360, ChronoUnit.DAYS), "field");

        assertThat(response, is("field" + LESS_THAN_ONE_YEAR_AGO));
    }
}
