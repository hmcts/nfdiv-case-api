package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseAccessAdministrator;

@RequiredArgsConstructor
@Getter
public enum State {

    @CCD(
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    Draft("Draft"),

    @CCD(
        name = "20 week holding period",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    Holding("Holding"),

    @CCD(
        name = "AoS awaiting",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingAos("AwaitingAos"),

    @CCD(
        name = "AoS drafted",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AosDrafted("AosDrafted"),

    @CCD(
        name = "AoS overdue",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AosOverdue("AosOverdue"),

    @CCD(
        name = "Applicant 2 approved",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n"
    )
    Applicant2Approved("Applicant2Approved"),

    @CCD(
        name = "Application awaiting payment",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n"
    )
    AwaitingPayment("AwaitingPayment"),

    @CCD(
        name = "Application rejected",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    Rejected("Rejected"),

    @CCD(
        name = "Application withdrawn",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    Withdrawn("Withdrawn"),

    @CCD(
        name = "Awaiting admin clarification",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingAdminClarification("AwaitingAdminClarification"),

    @CCD(
        name = "Awaiting alternative service",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingAlternativeService("AwaitingAlternativeService"),

    @CCD(
        name = "Awaiting amended application",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingAmendedApplication("AwaitingAmendedApplication"),

    @CCD(
        name = "Awaiting applicant",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n"
    )
    AwaitingDocuments("AwaitingDocuments"),

    @CCD(
        name = "Awaiting applicant 1 response",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n"
    )
    AwaitingApplicant1Response("AwaitingApplicant1Response"),

    @CCD(
        name = "Awaiting applicant 2 response",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n"
    )
    AwaitingApplicant2Response("AwaitingApplicant2Response"),

    @CCD(
        name = "Awaiting bailiff referral",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingBailiffReferral("AwaitingBailiffReferral"),

    @CCD(
        name = "Awaiting bailiff service",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingBailiffService("AwaitingBailiffService"),

    @CCD(
        name = "Awaiting clarification",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingClarification("AwaitingClarification"),

    @CCD(
        name = "Awaiting conditional order",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingConditionalOrder("AwaitingConditionalOrder"),

    @CCD(
        name = "Awaiting DWP response",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingDwpResponse("AwaitingDWPResponse"),

    @CCD(
        name = "Awaiting Final Order",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingFinalOrder("AwaitingFinalOrder"),

    @CCD(
        name = "Awaiting general consideration",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingGeneralConsideration("AwaitingGeneralConsideration"),

    @CCD(
        name = "Awaiting general referral payment",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingGeneralReferralPayment("AwaitingGeneralReferralPayment"),

    @CCD(
        name = "Awaiting HWF decision",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n"
    )
    AwaitingHWFDecision("AwaitingHWFDecision"),

    @CCD(
        name = "Awaiting joint Conditional Order",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    ConditionalOrderPending("ConditionalOrderPending"),

    @CCD(
        name = "Awaiting judge clarification",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingJudgeClarification("AwaitingJudgeClarification"),

    @CCD(
        name = "Awaiting legal advisor referral",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingLegalAdvisorReferral("AwaitingLegalAdvisorReferral"),

    @CCD(
        name = "Awaiting service",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n"
    )
    AwaitingService("AwaitingService"),

    @CCD(
        name = "Awaiting Service Consideration",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingServiceConsideration("AwaitingServiceConsideration"),

    @CCD(
        name = "Awaiting service payment",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingServicePayment("AwaitingServicePayment"),

    @CCD(
        name = "Clarification response submitted",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    ClarificationSubmitted("ClarificationSubmitted"),

    @CCD(
        name = "Conditional order drafted",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    ConditionalOrderDrafted("ConditionalOrderDrafted"),

    @CCD(
        name = "Conditional order pronounced",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    ConditionalOrderPronounced("ConditionalOrderPronounced"),

    @CCD(
        name = "Conditional order refused",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    ConditionalOrderRefused("ConditionalOrderRefused"),

    @CCD(
        name = "Disputed divorce",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    Disputed("Disputed"),

    @CCD(
        name = "Final order complete",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    FinalOrderComplete("FinalOrderComplete"),

    @CCD(
        name = "Final order overdue",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    FinalOrderOverdue("FinalOrderOverdue"),

    @CCD(
        name = "Final order pending",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    FinalOrderPending("FinalOrderPending"),

    @CCD(
        name = "Final order requested",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    FinalOrderRequested("FinalOrderRequested"),

    @CCD(
        name = "General consideration complete",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    GeneralConsiderationComplete("GeneralConsiderationComplete"),

    @CCD(
        name = "Issued To bailiff",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    IssuedToBailiff("IssuedToBailiff"),

    @CCD(
        name = "Listed; awaiting pronouncement",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    AwaitingPronouncement("AwaitingPronouncement"),

    @CCD(
        name = "New paper case",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n"
    )
    NewPaperCase("NewPaperCase"),

    @CCD(
        name = "Offline document received by CW"
    )
    OfflineDocumentReceived("OfflineDocumentReceived"),

    @CCD(
        name = "Removed from bulk case",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {CaseAccessAdministrator.class}
    )
    BulkCaseReject("BulkCaseReject"),

    @CCD(
        name = "Submitted",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n\n"
    )
    Submitted("Submitted");

    private final String name;

}

