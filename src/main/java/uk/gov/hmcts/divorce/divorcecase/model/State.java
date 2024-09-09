package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultStateAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultStateAccessExcludingCAA;
import uk.gov.hmcts.divorce.divorcecase.model.access.LegalAdvisorAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SolicitorAccess;

import java.util.EnumSet;

@RequiredArgsConstructor
@Getter
public enum State {

    @CCD(
        label = "20 week holding period",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    Holding,

    @CCD(
        label = "AoS awaiting",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingAos,

    @CCD(
        label = "AoS drafted",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AosDrafted,

    @CCD(
        label = "AoS overdue",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AosOverdue,

    @CCD(
        label = "Applicant 2 approved",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    Applicant2Approved,

    @CCD(
        label = "Application awaiting payment",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingPayment,

    @CCD(
        label = "Application rejected",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    Rejected,

    @CCD(
        label = "Application withdrawn",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    Withdrawn,

    @CCD(
        label = "Archived",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {SolicitorAccess.class}
    )
    Archived,

    @CCD(
        label = "Awaiting admin clarification",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingAdminClarification,

    @CCD(
        label = "Awaiting alternative service",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingAlternativeService,

    @CCD(
        label = "Awaiting amended application",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingAmendedApplication,

    @CCD(
        label = "Awaiting applicant",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingDocuments,

    @CCD(
        label = "Awaiting applicant 1 response",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    AwaitingApplicant1Response,

    @CCD(
        label = "Awaiting applicant 2 response",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    AwaitingApplicant2Response,

    @CCD(
        label = "Awaiting bailiff referral",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingBailiffReferral,

    @CCD(
        label = "Awaiting bailiff service",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingBailiffService,

    @CCD(
        label = "Awaiting clarification",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingClarification,

    @CCD(
        label = "Awaiting conditional order",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingConditionalOrder,

    @CCD(
        label = "Awaiting DWP response",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingDwpResponse,

    @CCD(
        label = "Awaiting final order",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingFinalOrder,

    @CCD(
        label = "Awaiting general consideration",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralConsideration,

    @CCD(
        label = "Awaiting general referral payment",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralReferralPayment,

    @CCD(
        label = "Awaiting HWF decision",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHWFDecision,

    @CCD(
        label = "Awaiting HWF evidence",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHWFEvidence,

    @CCD(
        label = "Awaiting HWF part payment",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHWFPartPayment,

    @CCD(
        label = "Awaiting joint conditional order",
        hint = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderPending,

    @CCD(
        label = "Awaiting joint final order",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingJointFinalOrder,

    @CCD(
        label = "Awaiting judge clarification",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingJudgeClarification,

    @CCD(
        label = "Awaiting legal advisor referral",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingLegalAdvisorReferral,

    @CCD(
        label = "Awaiting respondent final order payment",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingFinalOrderPayment,

    @CCD(
        label = "Awaiting service",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingService,

    @CCD(
        label = "Awaiting service consideration",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingServiceConsideration,

    @CCD(
        label = "Awaiting service payment",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    AwaitingServicePayment,

    @CCD(
        label = "AwaitingAnswer",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingAnswer,

    @CCD(
        label = "AwaitingJS/Nullity",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingJsNullity,

    @CCD(
        label = "Bailiff service refused",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    BailiffRefused,

    @CCD(
        label = "Case in bulk action process",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    InBulkActionCase,

    @CCD(
        label = "Clarification response submitted",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ClarificationSubmitted,

    @CCD(
        label = "Conditional order drafted",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderDrafted,

    @CCD(
        label = "Conditional order pronounced",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderPronounced,

    @CCD(
        label = "Conditional order refused",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderRefused,

    @CCD(
            label = "Conditional order review caseworker",
            hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
            access = {DefaultStateAccess.class}
    )
    ConditionalOrderReview,

    @CCD(
        label = "Draft",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class, SolicitorAccess.class}
    )
    Draft,

    @CCD(
        label = "Final order complete",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    FinalOrderComplete,

    @CCD(
        label = "Final order pending",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    FinalOrderPending,

    @CCD(
        label = "Final order requested",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    FinalOrderRequested,

    @CCD(
        label = "General application received",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    GeneralApplicationReceived,

    @CCD(
        label = "General consideration complete",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    GeneralConsiderationComplete,

    @CCD(
        label = "Issued to bailiff",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    IssuedToBailiff,

    @CCD(
        label = "Judicial Separation, Awaiting legal advisor",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    JSAwaitingLA,

    @CCD(
        label = "LA Review",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    LAReview,

    @CCD(
        label = "Listed; awaiting pronouncement",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingPronouncement,

    @CCD(
        label = "New paper case",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    NewPaperCase,

    @CCD(
        label = "Offline document received by CW",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    OfflineDocumentReceived,

    @CCD(
        label = "Pending hearing date",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    PendingHearingDate,

    @CCD(
        label = "Pending hearing outcome",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    PendingHearingOutcome,

    @CCD(
        label = "Removed from bulk case",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    BulkCaseReject,

    @CCD(
        label = "Respondent Final order requested",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    RespondentFinalOrderRequested,

    @CCD(
        label = "Separation order granted",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    SeparationOrderGranted,

    @CCD(
        label = "Service Admin Refusal",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    ServiceAdminRefusal,

    @CCD(
        label = "Submitted",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccessExcludingCAA.class}
    )
    Submitted,

    @CCD(
        label = "Welsh Translation requested",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    WelshTranslationRequested,

    @CCD(
        label = "Welsh Translation review",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    WelshTranslationReview;

    public static final EnumSet<State> POST_SUBMISSION_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved,
        Withdrawn,
        Rejected
    ));

    public static final EnumSet<State> PRE_SUBMISSION_STATES = EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved,
        AwaitingPayment,
        AwaitingHWFDecision,
        AwaitingDocuments
    );

    public static final EnumSet<State> POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved
    ));

    public static final EnumSet<State> STATES_NOT_WITHDRAWN_OR_REJECTED = EnumSet.complementOf(EnumSet.of(
        Withdrawn,
        Rejected
    ));

    public static final EnumSet<State> PRE_RETURN_TO_PREVIOUS_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved,
        Withdrawn,
        AwaitingClarification,
        AwaitingAmendedApplication
    ));

    public static final EnumSet<State> POST_ISSUE_STATES = EnumSet.complementOf(EnumSet.of(
        Draft,
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved,
        AwaitingPayment,
        AwaitingHWFDecision,
        AwaitingDocuments,
        Submitted,
        Withdrawn,
        Rejected
    ));

    public static final State[] AOS_STATES = {
        Holding, AwaitingConditionalOrder, IssuedToBailiff, AwaitingBailiffService, AwaitingBailiffReferral, BailiffRefused,
        AwaitingServiceConsideration, AwaitingServicePayment, AwaitingAlternativeService, AwaitingDwpResponse,
        AwaitingJudgeClarification, GeneralConsiderationComplete, AwaitingGeneralReferralPayment, AwaitingGeneralConsideration,
        GeneralApplicationReceived, PendingHearingOutcome, PendingHearingDate
    };

    public static final State[] POST_SUBMISSION_PRE_AWAITING_CO_STATES = {
        Submitted,
        AwaitingService,
        AwaitingAos,
        AwaitingServicePayment,
        AwaitingServiceConsideration,
        AwaitingBailiffReferral,
        BailiffRefused,
        AosOverdue,
        AosDrafted,
        AwaitingBailiffService,
        IssuedToBailiff
    };
}

