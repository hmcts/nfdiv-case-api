package uk.gov.hmcts.divorce.divorcecase.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.ERROR_CASE_NOT_ELIGIBLE;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.ERROR_FO_DATE_IN_FUTURE;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.ERROR_FO_GRANTED_EARLIER_THAN_CO_GRANTED;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.ERROR_FO_NOT_GRANTED;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.ERROR_TOO_EARLY_FOR_RESPONDENT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.ErrorsAndWarnings;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.WARNING_FO_GRANTED_NOT_WITHIN_CURRENT_CALENDAR_YEAR;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.validateFinalOrderGrantedDate;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.validateFinalOrderGrantedDateWithEligibility;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.EMPTY;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class FinalOrderValidationTest {
    @Test
    void shouldReturnValidationErrorWhenEligibleDateIsInTheFuture() {
        final CaseData caseData = caseData();
        LocalDate futureDate = LocalDate.now().plusDays(5);

        caseData.getFinalOrder().setDateFinalOrderEligibleToRespondent(futureDate);

        final List<String> result =
            FinalOrderValidation.validateCanRespondentApplyFinalOrder(caseData);

        assertThat(result)
            .containsExactly(
                String.format(
                    ERROR_TOO_EARLY_FOR_RESPONDENT_FINAL_ORDER,
                    DATE_TIME_FORMATTER.format(futureDate)
                )
            );
    }

    @Test
    void shouldNotReturnValidationErrorWhenEligibleDateIsToday() {
        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDateFinalOrderEligibleToRespondent(LocalDate.now());

        final List<String> result =
            FinalOrderValidation.validateCanRespondentApplyFinalOrder(caseData);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnValidationErrorWhenEligibleDateIsInThePast() {
        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDateFinalOrderEligibleToRespondent(LocalDate.now().minusDays(1));

        final List<String> result =
            FinalOrderValidation.validateCanRespondentApplyFinalOrder(caseData);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenCoHasNotBeenSubmitted() {
        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        ErrorsAndWarnings errorsAndWarnings = validateFinalOrderGrantedDate(caseDetails);

        assertThat(errorsAndWarnings.warnings).isEmpty();
        assertThat(errorsAndWarnings.errors).isNotEmpty();
        assertThat(errorsAndWarnings.errors.size()).isEqualTo(3);
        assertThat(errorsAndWarnings.errors).contains("conditionalOrderGrantedDate" + EMPTY);
        assertThat(errorsAndWarnings.errors).contains(ERROR_FO_NOT_GRANTED);
        assertThat(errorsAndWarnings.errors).contains("finalOrderGrantedDate" + EMPTY);
    }

    @Test
    void shouldReturnErrorsWhenFoHasNotBeenSubmitted() {
        LocalDate coGrantedDate = LocalDate.now();

        CaseData caseData = caseData();
        caseData.getConditionalOrder().setGranted(YES);
        caseData.getConditionalOrder().setGrantedDate(coGrantedDate);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        ErrorsAndWarnings errorsAndWarnings = validateFinalOrderGrantedDate(caseDetails);

        assertThat(errorsAndWarnings.warnings).isEmpty();
        assertThat(errorsAndWarnings.errors).isNotEmpty();
        assertThat(errorsAndWarnings.errors.size()).isEqualTo(2);
        assertThat(errorsAndWarnings.errors).contains(ERROR_FO_NOT_GRANTED);
        assertThat(errorsAndWarnings.errors).contains("finalOrderGrantedDate" + EMPTY);
    }

    @Test
    void shouldReturnErrorWhenFoGrantedDateIsBeforeCoGrantedDate() {
        LocalDate coGrantedDate = LocalDate.now();
        LocalDateTime foGrantedDate = LocalDateTime.now().minusDays(1);

        CaseData caseData = caseData();
        caseData.getConditionalOrder().setGranted(YES);
        caseData.getConditionalOrder().setGrantedDate(coGrantedDate);
        caseData.getFinalOrder().setGranted(Set.of(FinalOrder.Granted.YES));
        caseData.getFinalOrder().setGrantedDate(foGrantedDate);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        ErrorsAndWarnings errorsAndWarnings = validateFinalOrderGrantedDate(caseDetails);

        assertThat(errorsAndWarnings.warnings).isEmpty();
        assertThat(errorsAndWarnings.errors).isNotEmpty();
        assertThat(errorsAndWarnings.errors.size()).isEqualTo(1);
        assertThat(errorsAndWarnings.errors).contains(ERROR_FO_GRANTED_EARLIER_THAN_CO_GRANTED);
    }

    @Test
    void shouldReturnErrorWhenFoGrantedDateIsAfterCurrentDate() {
        LocalDate coGrantedDate = LocalDate.now().minusDays(1);
        LocalDateTime foGrantedDate = LocalDateTime.now().plusDays(1);

        CaseData caseData = caseData();
        caseData.getConditionalOrder().setGranted(YES);
        caseData.getConditionalOrder().setGrantedDate(coGrantedDate);
        caseData.getFinalOrder().setGranted(Set.of(FinalOrder.Granted.YES));
        caseData.getFinalOrder().setGrantedDate(foGrantedDate);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        ErrorsAndWarnings errorsAndWarnings = validateFinalOrderGrantedDate(caseDetails);

        assertThat(errorsAndWarnings.warnings).isEmpty();
        assertThat(errorsAndWarnings.errors).isNotEmpty();
        assertThat(errorsAndWarnings.errors.size()).isEqualTo(1);
        assertThat(errorsAndWarnings.errors).contains(ERROR_FO_DATE_IN_FUTURE);
    }

    @Test
    void shouldReturnWarningWhenFoGrantedDateNotInCurrentCalendarYear() {
        LocalDate coGrantedDate = LocalDate.now().minusYears(2);
        LocalDateTime foGrantedDate = LocalDateTime.now().minusYears(1);

        CaseData caseData = caseData();
        caseData.getConditionalOrder().setGranted(YES);
        caseData.getConditionalOrder().setGrantedDate(coGrantedDate);
        caseData.getFinalOrder().setGranted(Set.of(FinalOrder.Granted.YES));
        caseData.getFinalOrder().setGrantedDate(foGrantedDate);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        ErrorsAndWarnings errorsAndWarnings = validateFinalOrderGrantedDate(caseDetails);

        assertThat(errorsAndWarnings.errors).isEmpty();
        assertThat(errorsAndWarnings.warnings).isNotEmpty();
        assertThat(errorsAndWarnings.warnings.size()).isEqualTo(1);
        assertThat(errorsAndWarnings.warnings).contains(WARNING_FO_GRANTED_NOT_WITHIN_CURRENT_CALENDAR_YEAR);
    }

    @Test
    void shouldReturnErrorWhenFoGrantedDateIsBeforeEligibleDate() {
        LocalDateTime foGrantedDate = LocalDateTime.now();
        LocalDate coGrantedDate = foGrantedDate.toLocalDate().minusDays(1);
        LocalDate foEligibleDate = foGrantedDate.toLocalDate().plusDays(1);

        CaseData caseData = caseData();
        caseData.getConditionalOrder().setGranted(YES);
        caseData.getConditionalOrder().setGrantedDate(coGrantedDate);
        caseData.getFinalOrder().setGranted(Set.of(FinalOrder.Granted.YES));
        caseData.getFinalOrder().setGrantedDate(foGrantedDate);
        caseData.getFinalOrder().setDateFinalOrderEligibleFrom(foEligibleDate);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        ErrorsAndWarnings errorsAndWarnings = validateFinalOrderGrantedDateWithEligibility(caseDetails);

        assertThat(errorsAndWarnings.warnings).isEmpty();
        assertThat(errorsAndWarnings.errors).isNotEmpty();
        assertThat(errorsAndWarnings.errors.size()).isEqualTo(1);
        assertThat(errorsAndWarnings.errors).contains(ERROR_CASE_NOT_ELIGIBLE);
    }

    @Test
    void shouldReturnErrorWhenFoEligibleDateNotSet() {
        LocalDate coGrantedDate = LocalDate.now().minusDays(1);
        LocalDateTime foGrantedDate = LocalDateTime.now();

        CaseData caseData = caseData();
        caseData.getConditionalOrder().setGranted(YES);
        caseData.getConditionalOrder().setGrantedDate(coGrantedDate);
        caseData.getFinalOrder().setGranted(Set.of(FinalOrder.Granted.YES));
        caseData.getFinalOrder().setGrantedDate(foGrantedDate);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        ErrorsAndWarnings errorsAndWarnings = validateFinalOrderGrantedDateWithEligibility(caseDetails);

        assertThat(errorsAndWarnings.warnings).isEmpty();
        assertThat(errorsAndWarnings.errors).isNotEmpty();
        assertThat(errorsAndWarnings.errors.size()).isEqualTo(1);
        assertThat(errorsAndWarnings.errors).contains("dateFinalOrderEligibleFrom" + EMPTY);
    }
}
