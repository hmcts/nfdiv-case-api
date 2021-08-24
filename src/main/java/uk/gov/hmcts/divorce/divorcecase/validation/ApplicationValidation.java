package uk.gov.hmcts.divorce.divorcecase.validation;

import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant2BasicCase;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateBasicCase;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCaseFieldsForIssueApplication;

public final class ApplicationValidation {

    private ApplicationValidation() {

    }

    public static List<String> validateReadyForPayment(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        validateBasicCase(caseData, errors);
        if (caseData.getApplicationType() != null && !caseData.getApplicationType().isSole()) {
            validateApplicant2BasicCase(caseData, errors);
        }
        return errors;
    }

    public static List<String> validateSubmission(Application application) {
        List<String> errors = new ArrayList<>();

        if (!application.hasBeenPaidFor()) {
            errors.add("Payment incomplete");
        }

        if (!application.applicant1HasStatementOfTruth() && !application.hasSolSignStatementOfTruth()) {
            errors.add("Statement of truth must be accepted by the person making the application");
        }

        return errors;
    }

    public static List<String> validateIssue(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        validateBasicCase(caseData, errors);
        validateCaseFieldsForIssueApplication(caseData.getApplication().getMarriageDetails(), errors);
        return errors;
    }

}
