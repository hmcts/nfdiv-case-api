package uk.gov.hmcts.divorce.divorcecase.validation;

import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

public final class FinalOrderValidation {
    private FinalOrderValidation() {

    }

    public static final String ERROR_TOO_EARLY_FOR_RESPONDENT_FINAL_ORDER =
        "It’s too early to apply for a final order on behalf of the respondent. You will be able to apply from %s.";

    public static List<String> validateCanRespondentApplyFinalOrder(CaseData caseData) {
        final LocalDate currentDate = LocalDate.now();
        final LocalDate dateFinalOrderEligible = caseData.getFinalOrder().getDateFinalOrderEligibleToRespondent();

        if (dateFinalOrderEligible == null || dateFinalOrderEligible.isAfter(currentDate)) {
            final String formattedDate = dateFinalOrderEligible == null ? "" : DATE_TIME_FORMATTER.format(dateFinalOrderEligible);

            return List.of(String.format(ERROR_TOO_EARLY_FOR_RESPONDENT_FINAL_ORDER, formattedDate));
        }

        return Collections.emptyList();
    }
}
