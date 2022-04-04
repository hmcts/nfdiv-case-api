package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultStateAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultStateAccessExcludingCAA;
import uk.gov.hmcts.divorce.divorcecase.model.access.SolicitorAccess;

import java.util.EnumSet;

@RequiredArgsConstructor
@Getter
public enum State {

    @CCD(
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class, SolicitorAccess.class}
    )
    Draft("Draft"),

    @CCD(
        name = "20 week holding period",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    Holding("Holding"),

    @CCD(
        name = "AoS awaiting",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingAos("AwaitingAos"),

    @CCD(
        name = "AoS drafted",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AosDrafted("AosDrafted"),

    @CCD(
        name = "AoS overdue",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AosOverdue("AosOverdue"),

    @CCD(
        name = "Applicant 2 approved",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    Applicant2Approved("Applicant2Approved"),

    @CCD(
        name = "Application awaiting payment",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingPayment("AwaitingPayment"),

    @CCD(
        name = "Application rejected",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    Rejected("Rejected"),

    @CCD(
        name = "Application withdrawn",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    Withdrawn("Withdrawn"),

    @CCD(
        name = "Awaiting admin clarification",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingAdminClarification("AwaitingAdminClarification"),

    @CCD(
        name = "Awaiting alternative service",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingAlternativeService("AwaitingAlternativeService"),

    @CCD(
        name = "Awaiting amended application",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingAmendedApplication("AwaitingAmendedApplication"),

    @CCD(
        name = "Awaiting applicant",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingDocuments("AwaitingDocuments"),

    @CCD(
        name = "Awaiting applicant 1 response",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    AwaitingApplicant1Response("AwaitingApplicant1Response"),

    @CCD(
        name = "Awaiting applicant 2 response",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    AwaitingApplicant2Response("AwaitingApplicant2Response"),

    @CCD(
        name = "Awaiting bailiff referral",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingBailiffReferral("AwaitingBailiffReferral"),

    @CCD(
        name = "Awaiting bailiff service",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingBailiffService("AwaitingBailiffService"),

    @CCD(
        name = "Awaiting clarification",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingClarification("AwaitingClarification"),

    @CCD(
        name = "Awaiting conditional order",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingConditionalOrder("AwaitingConditionalOrder"),

    @CCD(
        name = "Awaiting DWP response",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingDwpResponse("AwaitingDWPResponse"),

    @CCD(
        name = "Awaiting final order",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingFinalOrder("AwaitingFinalOrder"),

    @CCD(
        name = "Awaiting general consideration",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralConsideration("AwaitingGeneralConsideration"),

    @CCD(
        name = "Awaiting general referral payment",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralReferralPayment("AwaitingGeneralReferralPayment"),

    @CCD(
        name = "Awaiting HWF decision",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHWFDecision("AwaitingHWFDecision"),

    @CCD(
        name = "Awaiting joint conditional order",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderPending("ConditionalOrderPending"),

    @CCD(
        name = "Awaiting judge clarification",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingJudgeClarification("AwaitingJudgeClarification"),

    @CCD(
        name = "Awaiting legal advisor referral",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingLegalAdvisorReferral("AwaitingLegalAdvisorReferral"),

    @CCD(
        name = "Awaiting service",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingService("AwaitingService"),

    @CCD(
        name = "Awaiting service consideration",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingServiceConsideration("AwaitingServiceConsideration"),

    @CCD(
        name = "Awaiting service payment",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingServicePayment("AwaitingServicePayment"),

    @CCD(
        name = "Clarification response submitted",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ClarificationSubmitted("ClarificationSubmitted"),

    @CCD(
        name = "Conditional order drafted",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderDrafted("ConditionalOrderDrafted"),

    @CCD(
        name = "Conditional order pronounced",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderPronounced("ConditionalOrderPronounced"),

    @CCD(
        name = "Conditional order refused",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderRefused("ConditionalOrderRefused"),

    @CCD(
        name = "Disputed divorce",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    Disputed("Disputed"),

    @CCD(
        name = "Final order complete",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    FinalOrderComplete("FinalOrderComplete"),

    @CCD(
        name = "Final order overdue",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    FinalOrderOverdue("FinalOrderOverdue"),

    @CCD(
        name = "Final order pending",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    FinalOrderPending("FinalOrderPending"),

    @CCD(
        name = "Final order requested",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    FinalOrderRequested("FinalOrderRequested"),

    @CCD(
        name = "General application received",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    GeneralApplicationReceived("GeneralApplicationReceived"),

    @CCD(
        name = "General consideration complete",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    GeneralConsiderationComplete("GeneralConsiderationComplete"),

    @CCD(
        name = "Issued to bailiff",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    IssuedToBailiff("IssuedToBailiff"),

    @CCD(
        name = "Listed; awaiting pronouncement",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingPronouncement("AwaitingPronouncement"),

    @CCD(
        name = "New paper case",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    NewPaperCase("NewPaperCase"),

    @CCD(
        name = "Offline document received by CW",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    OfflineDocumentReceived("OfflineDocumentReceived"),

    @CCD(
        name = "Removed from bulk case",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    BulkCaseReject("BulkCaseReject"),

    @CCD(
        name = "Submitted",
        label = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    Submitted("Submitted");

    public static final EnumSet<State> PRE_CONDITIONAL_ORDER_STATES = EnumSet.of(
        AwaitingHWFDecision,
        AwaitingPayment,
        AwaitingDocuments,
        Submitted,
        AwaitingAos,
        AwaitingService,
        AosOverdue,
        AosDrafted,
        AwaitingServicePayment,
        AwaitingServiceConsideration,
        AwaitingBailiffReferral,
        AwaitingBailiffService,
        IssuedToBailiff,
        Holding,
        AwaitingConditionalOrder
    );

    public static final EnumSet<State> POST_SUBMISSION_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved
    ));

    public static final EnumSet<State> POST_ISSUE_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved,
        AwaitingPayment,
        AwaitingHWFDecision,
        AwaitingDocuments,
        Submitted
    ));

    public static final State[] AOS_STATES = {
        Holding, AwaitingConditionalOrder, IssuedToBailiff, AwaitingBailiffService, AwaitingBailiffReferral,
        AwaitingServiceConsideration, AwaitingServicePayment, AwaitingAlternativeService, AwaitingDwpResponse,
        AwaitingJudgeClarification, GeneralConsiderationComplete, AwaitingGeneralReferralPayment, AwaitingGeneralConsideration,
        GeneralApplicationReceived
    };

    private final String name;

}

