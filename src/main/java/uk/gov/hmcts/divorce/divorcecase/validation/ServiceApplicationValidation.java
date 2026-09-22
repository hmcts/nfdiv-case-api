package uk.gov.hmcts.divorce.divorcecase.validation;

import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class ServiceApplicationValidation {

    private ServiceApplicationValidation() {
    }

    public static final String ERROR_ALREADY_SUBMITTED =
        "The ongoing service application on this case has already been submitted and you cannot submit it again or amend it.";

    public static List<String> validateNotAlreadySubmitted(CaseData caseData) {
        return isAlreadySubmitted(caseData) ? singletonList(ERROR_ALREADY_SUBMITTED) : emptyList();
    }

    public static boolean isAlreadySubmitted(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getAlternativeService)
            .map(AlternativeService::getServicePaymentFee)
            .map(ServiceApplicationValidation::hasAnySubmissionReference)
            .orElse(false);
    }

    private static boolean hasAnySubmissionReference(FeeDetails fee) {
        return isNotBlank(fee.getPaymentReference()) || isNotBlank(fee.getHelpWithFeesReferenceNumber());
    }
}
