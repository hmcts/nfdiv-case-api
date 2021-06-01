package uk.gov.hmcts.divorce.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.hasAwaitingDocuments;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.isPaymentIncomplete;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateBasicCase;

@RequiredArgsConstructor
@Getter
public enum State {

    @CCD(
        name = "Draft",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    Draft("Draft"),

    @CCD(
        name = "Awaiting payment",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    AwaitingPayment("AwaitingPayment") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();
            validateBasicCase(caseData, errors);
            return errors;
        }
    },

    @CCD(
        name = "Application paid and submitted - awaiting documents",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    AwaitingDocuments("AwaitingDocuments") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();

            if (isPaymentIncomplete(caseData)) {
                errors.add("Payment incomplete");
            }
            if (!hasAwaitingDocuments(caseData)) {
                errors.add("No Awaiting documents");
            }

            return errors;
        }
    },

    @CCD(
        name = "Application submitted and awaiting HWF decision",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    AwaitingHWFDecision("AwaitingHWFDecision") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();

            if (caseData.getHelpWithFeesAppliedForFees().toBoolean()
                && caseData.getHelpWithFeesReferenceNumber().isEmpty()) {
                errors.add("Incomplete HWF reference number");
            }

            return errors;
        }
    },

    @CCD(
        name = "Application paid and submitted",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    Submitted("Submitted") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();

            if (isPaymentIncomplete(caseData)) {
                errors.add("Payment incomplete");
            }
            if (hasAwaitingDocuments(caseData)) {
                errors.add("Awaiting documents");
            }
            if (!caseData.hasStatementOfTruth() && !caseData.hasSolSignStatementOfTruth()) {
                errors.add("Statement of truth must be accepted by the person making the application");
            }

            return errors;
        }
    };

    private final String name;

    public List<String> validate(CaseData data) {
        return null;
    }

}

