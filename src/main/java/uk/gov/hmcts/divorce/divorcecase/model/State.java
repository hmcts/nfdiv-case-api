package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseAccessAdministrator;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.isPaymentIncomplete;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant1BasicCase;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant2BasicCase;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateBasicCase;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCaseFieldsForIssueApplication;

@RequiredArgsConstructor
@Getter
public enum State {

    @CCD(
        name = "Draft",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    Draft("Draft"),

    @CCD(
        name = "Awaiting applicant 1 response",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    AwaitingApplicant1Response("AwaitingApplicant1Response"),

    @CCD(
        name = "Awaiting applicant 2 response",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    AwaitingApplicant2Response("AwaitingApplicant2Response") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();
            validateApplicant1BasicCase(caseData, errors);
            return errors;
        }
    },

    @CCD(
        name = "Applicant 2 approved",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    Applicant2Approved("Applicant2Approved") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();
            validateApplicant2BasicCase(caseData, errors);
            return errors;
        }
    },

    @CCD(
        name = "Application awaiting payment",
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
        name = "Awaiting applicant",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    AwaitingDocuments("AwaitingDocuments") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();

            if (isPaymentIncomplete(caseData)) {
                errors.add("Payment incomplete");
            }
            if (!caseData.getApplication().hasAwaitingDocuments()) {
                errors.add("No Awaiting documents");
            }

            return errors;
        }
    },

    @CCD(
        name = "Awaiting HWF decision",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    AwaitingHWFDecision("AwaitingHWFDecision"),

    @CCD(
        name = "Submitted",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n"
    )
    Submitted("Submitted") {
        @Override
        public List<String> validate(CaseData caseData) {
            List<String> errors = new ArrayList<>();

            if (isPaymentIncomplete(caseData)) {
                errors.add("Payment incomplete");
            }
            if (caseData.getApplication().hasAwaitingDocuments()) {
                errors.add("Awaiting documents");
            }
            if (!caseData.getApplication().applicant1HasStatementOfTruth() && !caseData.getApplication().hasSolSignStatementOfTruth()) {
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
            validateCaseFieldsForIssueApplication(caseData.getApplication().getMarriageDetails(), errors);
            return errors;
        }
    },

    @CCD(
        name = "AOS awaiting",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingAos("AwaitingAos"),

    @CCD(
        name = "AOS overdue",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AosOverdue("AosOverdue"),

    @CCD(
        name = "AOS drafted",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AosDrafted("AosDrafted"),

    @CCD(
        name = "Defended divorce",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    DefendedDivorce("DefendedDivorce"),

    @CCD(
        name = "20 week holding period",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    Holding("Holding"),

    @CCD(
        name = "Awaiting conditional order",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingConditionalOrder("AwaitingConditionalOrder"),

    @CCD(
        name = "Conditional order drafted",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    ConditionalOrderDrafted("ConditionalOrderDrafted"),

    @CCD(
        name = "Awaiting legal advisor referral",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingLegalAdvisorReferral("AwaitingLegalAdvisorReferral"),

    @CCD(
        name = "Awaiting clarification",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingClarification("AwaitingClarification"),

    @CCD(
        name = "Conditional order refused",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    ConditionalOrderRefused("ConditionalOrderRefused"),

    @CCD(
        name = "Listed; awaiting pronouncement",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingPronouncement("AwaitingPronouncement"),

    @CCD(
        name = "Conditional order pronounced",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    ConditionalOrderPronounced("ConditionalOrderPronounced"),

    @CCD(
        name = "Application rejected",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    Rejected("Rejected"),

    @CCD(
        name = "Application withdrawn",
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
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingReissue("AwaitingReissue"),

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

