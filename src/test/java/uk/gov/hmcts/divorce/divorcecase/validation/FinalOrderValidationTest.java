package uk.gov.hmcts.divorce.divorcecase.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.validation.FinalOrderValidation.ERROR_TOO_EARLY_FOR_RESPONDENT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
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
}
