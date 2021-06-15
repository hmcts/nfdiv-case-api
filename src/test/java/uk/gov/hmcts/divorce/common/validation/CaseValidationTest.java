package uk.gov.hmcts.divorce.common.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.payment.model.Payment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.addToErrorList;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfConfidentialAddressNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfDateIsAllowed;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfGenderNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfStringNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfYesOrNoIsNullOrEmptyOrNo;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.checkIfYesOrNoNullOrEmpty;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.hasAwaitingDocuments;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.isPaymentIncomplete;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateBasicCase;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateCaseFieldsForIssueApplication;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

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
        assertThat(errors.size(), is(13));
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
        String response = checkIfDateIsAllowed(LocalDate.now().plus(2, YEARS), "field");

        assertThat(response, is("field" + IN_THE_FUTURE));
    }

    @Test
    public void shouldReturnErrorWhenDateIsOverOneHundredYearsAgo() {
        LocalDate oneHundredYearsAndOneDayAgo = LocalDate.now()
            .minus(100, YEARS)
            .minus(1, DAYS);

        String response = checkIfDateIsAllowed(oneHundredYearsAndOneDayAgo, "field");

        assertThat(response, is("field" + MORE_THAN_ONE_HUNDRED_YEARS_AGO));
    }

    @Test
    public void shouldReturnErrorWhenDateIsLessThanOneYearAgo() {
        String response = checkIfDateIsAllowed(LocalDate.now().minus(360, DAYS), "field");

        assertThat(response, is("field" + LESS_THAN_ONE_YEAR_AGO));
    }

    @Test
    public void shouldReturnTrueWhenPaymentIsIncompleted() {
        CaseData caseData = new CaseData();
        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.setApplicationFeeOrderSummary(orderSummary);

        assertTrue(isPaymentIncomplete(caseData));
    }

    @Test
    public void shouldReturnFalseWhenPaymentIsCompleted() {
        CaseData caseData = new CaseData();
        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.setApplicationFeeOrderSummary(orderSummary);
        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(SUCCESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));
        assertFalse(isPaymentIncomplete(caseData));
    }

    @Test
    public void shouldReturnTrueWhenCaseHasAwaitingDocuments() {
        CaseData caseData = new CaseData();
        caseData.setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);
        assertTrue(hasAwaitingDocuments(caseData));
    }

    @Test
    public void shouldReturnFalseWhenCaseDoesNotHaveAwaitingDocuments() {
        CaseData caseData = new CaseData();
        assertFalse(hasAwaitingDocuments(caseData));
    }

    @Test
    public void shouldReturnErrorWhenApp2MarriageCertNameAndPlaceOfMarriageAreMissing() {
        CaseData caseData = new CaseData();
        List<String> errors = new ArrayList<>();

        validateCaseFieldsForIssueApplication(caseData, errors);

        Assertions.assertThat(errors).containsExactlyInAnyOrder(
            "MarriageApplicant2Name cannot be empty or null",
            "PlaceOfMarriage cannot be empty or null"
        );
    }

    @Test
    public void shouldReturnErrorWhenApp2MarriageCertNameIsMissing() {
        CaseData caseData = new CaseData();
        caseData.getMarriageDetails().setPlaceOfMarriage("London");
        List<String> errors = new ArrayList<>();

        validateCaseFieldsForIssueApplication(caseData, errors);

        Assertions.assertThat(errors).containsExactlyInAnyOrder(
            "MarriageApplicant2Name cannot be empty or null"
        );
    }

    @Test
    public void shouldNotReturnErrorWhenBothWhenApp2MarriageCertNameAndPlaceOfMarriageArePresent() {
        CaseData caseData = new CaseData();
        caseData.getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getMarriageDetails().setApplicant2Name("TestFname TestMname  TestLname");
        List<String> errors = emptyList();

        validateCaseFieldsForIssueApplication(caseData, errors);

        Assertions.assertThat(errors).isEmpty();
    }
}
