package uk.gov.hmcts.divorce.common.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.payment.model.Payment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateJurisdictionConnection;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

public class CaseValidationTest {

    private static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    private static final String EMPTY = " cannot be empty or null";
    private static final String MUST_BE_YES = " must be YES";
    private static final String IN_THE_FUTURE = " can not be in the future.";
    private static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";
    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";

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
    public void shouldReturnErrorWhenJurisdictionConnectionIsA() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESP_RESIDENT));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.PET_RESP_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsB() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESP_LAST_RESIDENT));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.PET_RESP_LAST_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsC() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.RESP_RESIDENT));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.RESP_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsD() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESIDENT_TWELVE_MONTHS));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.PET_RESIDENT_TWELVE_MONTHS + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsE() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESIDENT_SIX_MONTHS));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.PET_RESIDENT_SIX_MONTHS + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsF() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESP_DOMICILED));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.PET_RESP_DOMICILED + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsG() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.RESIDUAL_JURISDICTION));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsH() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_DOMICILED));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.PET_DOMICILED + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsI() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.RESP_DOMICILED));

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.RESP_DOMICILED + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionsIsNull() {
        CaseData caseData = new CaseData();

        List<String> errors = validateJurisdictionConnection(caseData);

        assertThat(errors, contains("JurisdictionConnections" + EMPTY));
    }

    @Test
    public void shouldReturnTrueWhenPaymentIsIncompleted() {
        CaseData caseData = new CaseData();
        assertTrue(isPaymentIncomplete(caseData));
    }

    @Test
    public void shouldReturnFalseWhenPaymentIsCompleted() {
        CaseData caseData = new CaseData();
        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(SUCCESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));
        assertFalse(isPaymentIncomplete(caseData));
    }

    @Test
    public void shouldReturnTrueWhenCaseHasAwaitingDocuments() {
        CaseData caseData = new CaseData();
        caseData.setPetitionerWantsToHavePapersServedAnotherWay(YesOrNo.YES);
        assertTrue(hasAwaitingDocuments(caseData));
    }

    @Test
    public void shouldReturnFaseWhenCaseDoesNotHaveAwaitingDocuments() {
        CaseData caseData = new CaseData();
        assertFalse(hasAwaitingDocuments(caseData));
    }
}
