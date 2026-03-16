package uk.gov.hmcts.divorce.divorcecase.validation;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.EMPTY;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.flattenLists;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.notNull;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

public final class FinalOrderValidation {
    private FinalOrderValidation() {

    }

    public static final String ERROR_TOO_EARLY_FOR_RESPONDENT_FINAL_ORDER =
        "It’s too early to apply for a final order on behalf of the respondent. You will be able to apply from %s.";
    public static final String ERROR_FO_GRANTED_EARLIER_THAN_CO_GRANTED =
        "The Final Order Granted date must be more recent than the Conditional Order Granted date.";
    public static final String ERROR_FO_NOT_GRANTED = "Final Order has not been granted";
    public static final String WARNING_FO_GRANTED_NOT_WITHIN_CURRENT_CALENDAR_YEAR =
        "The Final Order Granted date is not within the current year. Please verify the date before submitting.";
    public static final String ERROR_FO_DATE_IN_FUTURE = "The Final Order Granted date cannot be in the future.";
    public static final String ERROR_CASE_NOT_ELIGIBLE = "Case is not yet eligible for Final Order";

    public static List<String> validateCanRespondentApplyFinalOrder(CaseData caseData) {
        final LocalDate currentDate = LocalDate.now();
        final LocalDate dateFinalOrderEligible = caseData.getFinalOrder().getDateFinalOrderEligibleToRespondent();

        if (dateFinalOrderEligible == null || dateFinalOrderEligible.isAfter(currentDate)) {
            final String formattedDate = dateFinalOrderEligible == null ? "" : DATE_TIME_FORMATTER.format(dateFinalOrderEligible);

            return List.of(String.format(ERROR_TOO_EARLY_FOR_RESPONDENT_FINAL_ORDER, formattedDate));
        }

        return Collections.emptyList();
    }

    public static class ErrorsAndWarnings {
        public List<String> errors = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
    }

    private static ErrorsAndWarnings validateFoGrantedDateRequiredFields(CaseDetails<CaseData, State> details) {
        FinalOrder finalOrder = details.getData().getFinalOrder();
        ConditionalOrder conditionalOrder = details.getData().getConditionalOrder();
        ErrorsAndWarnings errorsAndWarnings = new ErrorsAndWarnings();
        errorsAndWarnings.errors = flattenLists(
            notNull(conditionalOrder.getGrantedDate(), "conditionalOrderGrantedDate"),
            !finalOrder.hasFinalOrderBeenGranted() ? singletonList(ERROR_FO_NOT_GRANTED) : emptyList(),
            notNull(finalOrder.getGrantedDate(), "finalOrderGrantedDate")
        );

        return errorsAndWarnings;
    }

    private static void validateFoGrantedDateExtraChecks(CaseDetails<CaseData, State> details, ErrorsAndWarnings errorsAndWarnings) {
        FinalOrder finalOrder = details.getData().getFinalOrder();
        ConditionalOrder conditionalOrder = details.getData().getConditionalOrder();

        if (errorsAndWarnings.errors.isEmpty()) {
            if (finalOrder.getGrantedDate().toLocalDate().isBefore(conditionalOrder.getGrantedDate())) {
                errorsAndWarnings.errors.add(ERROR_FO_GRANTED_EARLIER_THAN_CO_GRANTED);
            } else if (finalOrder.getGrantedDate().isAfter(LocalDateTime.now())) {
                errorsAndWarnings.errors.add(ERROR_FO_DATE_IN_FUTURE);
            } else if (finalOrder.getGrantedDate().getYear() != LocalDate.now().getYear()) {
                errorsAndWarnings.warnings.add(WARNING_FO_GRANTED_NOT_WITHIN_CURRENT_CALENDAR_YEAR);
            }
        }
    }

    public static ErrorsAndWarnings validateFinalOrderGrantedDate(CaseDetails<CaseData, State> details) {
        final ErrorsAndWarnings errorsAndWarnings = validateFoGrantedDateRequiredFields(details);
        validateFoGrantedDateExtraChecks(details, errorsAndWarnings);
        return errorsAndWarnings;
    }

    public static ErrorsAndWarnings validateFinalOrderGrantedDateWithEligibility(CaseDetails<CaseData, State> details) {
        final ErrorsAndWarnings errorsAndWarnings = validateFoGrantedDateRequiredFields(details);
        FinalOrder finalOrder = details.getData().getFinalOrder();
        LocalDate dateFinalOrderEligibleFrom = finalOrder.getDateFinalOrderEligibleFrom();
        LocalDateTime finalOrderGrantedDate = finalOrder.getGrantedDate();

        if (dateFinalOrderEligibleFrom == null) {
            errorsAndWarnings.errors.add("dateFinalOrderEligibleFrom" + EMPTY);
        }

        if (errorsAndWarnings.errors.isEmpty() && dateFinalOrderEligibleFrom.isAfter(finalOrderGrantedDate.toLocalDate())) {
            errorsAndWarnings.errors.add(ERROR_CASE_NOT_ELIGIBLE);
        }

        validateFoGrantedDateExtraChecks(details, errorsAndWarnings);

        return errorsAndWarnings;
    }
}
