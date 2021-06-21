package uk.gov.hmcts.divorce.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.common.model.access.CaseAccessAdministrator;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.hasAwaitingDocuments;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.isPaymentIncomplete;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateBasicCase;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.validateCaseFieldsForIssueApplication;

@RequiredArgsConstructor
@Getter
public enum State {

    @CCD(
        name = "Draft",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    Draft("Draft"),

    @CCD(
        name = "Awaiting Applicant 2 Response",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingApplicant2Response("AwaitingApplicant2Response"),

    @CCD(
        name = "Application awaiting payment",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
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
        name = "Awaiting applicant",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
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
        name = "Awaiting HWF decision",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingHWFDecision("AwaitingHWFDecision"),

    @CCD(
        name = "Submitted",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
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
    },

    @CCD(
        name = "Application issued",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    Issued("Issued") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();
            validateBasicCase(caseData, errors);
            validateCaseFieldsForIssueApplication(caseData, errors);
            return errors;
        }
    },

    @CCD(
        name = "Application Rejected",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    Rejected("Rejected"),

    @CCD(
        name = "Application Withdrawn",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    Withdrawn("Withdrawn"),

    @CCD(
        name = "Pending rejection",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    PendingRejection("PendingRejection"),

    @CCD(
        name = "Awaiting reissue",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    AwaitingReissue("AwaitingReissue"),

    @CCD(
        name = "Conditional order complete",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    ConditionalOrderComplete("ConditionalOrderComplete"),

    @CCD(
        name = "Final order complete",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    FinalOrderComplete("FinalOrderComplete");

    private final String name;

    public List<String> validate(CaseData data) {
        return null;
    }

}

