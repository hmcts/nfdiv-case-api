package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultStateAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultStateAccessExcludingCAA;
import uk.gov.hmcts.divorce.divorcecase.model.access.LegalAdvisorAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SolicitorAccess;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TITLE;

@RequiredArgsConstructor
@Getter
public enum State {

    @CCD(
        label = "20 week holding period",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    Holding,

    @CCD(
        label = "AoS awaiting",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingAos,

    @CCD(
        label = "AoS drafted",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AosDrafted,

    @CCD(
        label = "AoS overdue",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AosOverdue,

    @CCD(
        label = "Applicant 2 approved",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    Applicant2Approved,

    @CCD(
        label = "Application awaiting payment",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingPayment,

    @CCD(
        label = "Application rejected",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    Rejected,

    @CCD(
        label = "Application withdrawn",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    Withdrawn,

    @CCD(
        label = "Archived",
        hint = CASE_TITLE,
        access = {SolicitorAccess.class}
    )
    Archived,

    @CCD(
        label = "Awaiting admin clarification",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingAdminClarification,

    @CCD(
        label = "Awaiting alternative service",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingAlternativeService,

    @CCD(
        label = "Awaiting amended application",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingAmendedApplication,

    @CCD(
        label = "Awaiting applicant",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingDocuments,

    @CCD(
        label = "Awaiting applicant 1 response",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    AwaitingApplicant1Response,

    @CCD(
        label = "Awaiting applicant 2 response",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, SolicitorAccess.class}
    )
    AwaitingApplicant2Response,

    @CCD(
        label = "Awaiting bailiff referral",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingBailiffReferral,

    @CCD(
        label = "Awaiting bailiff service",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingBailiffService,

    @CCD(
        label = "Awaiting clarification",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingClarification,

    @CCD(
        label = "Awaiting conditional order",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingConditionalOrder,

    @CCD(
        label = "Awaiting DWP response",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingDwpResponse,

    @CCD(
        label = "Awaiting final order",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingFinalOrder,

    @CCD(
        label = "Awaiting GenAppHWF evidence",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingGenAppHWFEvidence,

    @CCD(
        label = "Awaiting GenAppHWF part payment",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingGenAppHWFPartPayment,

    @CCD(
        label = "Awaiting general application payment",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralApplicationPayment,

    @CCD(
        label = "Awaiting general consideration",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralConsideration,

    @CCD(
        label = "Awaiting general referral payment",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingGeneralReferralPayment,

    @CCD(
        label = "Awaiting HWF decision",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHWFDecision,

    @CCD(
        label = "Awaiting HWF evidence",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingHWFEvidence,

    @CCD(
        label = "Awaiting HWF part payment",
        hint = CASE_TITLE,
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
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingJointFinalOrder,

    @CCD(
        label = "Awaiting judge clarification",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingJudgeClarification,

    @CCD(
        label = "Awaiting legal advisor referral",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingLegalAdvisorReferral,

    @CCD(
        label = "Awaiting requested information",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingRequestedInformation,

    @CCD(
        label = "Awaiting respondent final order payment",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingFinalOrderPayment,


    @CCD(
        label = "Awaiting response to HWF Decision",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingResponseToHWFDecision,

    @CCD(
        label = "Awaiting service",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingService,

    @CCD(
        label = "Awaiting service consideration",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingServiceConsideration,

    @CCD(
        label = "Awaiting service payment",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    AwaitingServicePayment,

    @CCD(
        label = "AwaitingAnswer",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingAnswer,

    @CCD(
        label = "AwaitingJS/Nullity",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    AwaitingJsNullity,

    @CCD(
        label = "Bailiff service refused",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    BailiffRefused,

    @CCD(
        label = "Case in bulk action process",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    InBulkActionCase,

    @CCD(
        label = "Clarification response submitted",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    ClarificationSubmitted,

    @CCD(
        label = "Conditional order drafted",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderDrafted,

    @CCD(
        label = "Conditional order pronounced",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderPronounced,

    @CCD(
        label = "Conditional order refused",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    ConditionalOrderRefused,

    @CCD(
            label = "Conditional order review caseworker",
            hint = CASE_TITLE,
            access = {DefaultStateAccess.class}
    )
    ConditionalOrderReview,

    @CCD(
        label = "Draft",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class, SolicitorAccess.class}
    )
    Draft,

    @CCD(
        label = "Final order complete",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    FinalOrderComplete,

    @CCD(
        label = "Final order pending",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    FinalOrderPending,

    @CCD(
        label = "Final order requested",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    FinalOrderRequested,

    @CCD(
        label = "General application received",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    GeneralApplicationReceived,

    @CCD(
        label = "General consideration complete",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    GeneralConsiderationComplete,

    @CCD(
        label = "Information Requested",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    InformationRequested,

    @CCD(
        label = "Issued to bailiff",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    IssuedToBailiff,

    @CCD(
        label = "Judicial Separation, Awaiting legal advisor",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    JSAwaitingLA,

    @CCD(
        label = "LA Review",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    LAReview,

    @CCD(
        label = "LA service app review",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    LAServiceReview,

    @CCD(
        label = "Listed; awaiting pronouncement",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class, LegalAdvisorAccess.class}
    )
    AwaitingPronouncement,

    @CCD(
        label = "New paper case",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    NewPaperCase,

    @CCD(
        label = "Offline document received by CW",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    ) OfflineDocumentReceived,

    @CCD(
        label = "Pending hearing date",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    PendingHearingDate,

    @CCD(
        label = "Pending hearing outcome",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    PendingHearingOutcome,

    @CCD(
        label = "Pending refund",
        hint = "### Case number: ${hyphenatedCaseRef}\n ### ${applicant1LastName} and ${applicant2LastName}\n",
        access = {DefaultStateAccess.class}
    )
    PendingRefund,

    @CCD(
        label = "Pending service app response",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    PendingServiceAppResponse,

    @CCD(
        label = "Removed from bulk case",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    BulkCaseReject,

    @CCD(
        label = "Requested Information Submitted",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    )
    RequestedInformationSubmitted,

    @CCD(
        label = "Respondent Final order requested",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    RespondentFinalOrderRequested,

    @CCD(
        label = "Separation order granted",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    SeparationOrderGranted,

    @CCD(
        label = "Service Admin Refusal",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    ServiceAdminRefusal,

    @CCD(
        label = "Submitted",
        hint = CASE_TITLE,
        access = {DefaultStateAccessExcludingCAA.class}
    ) Submitted,

    @CCD(
        label = "Welsh Translation requested",
        hint = CASE_TITLE,
        access = {DefaultStateAccess.class}
    )
    WelshTranslationRequested,

    @CCD(
        label = "Welsh Translation review",
        hint = CASE_TITLE,
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
        AwaitingApplicant1Response,
        AwaitingApplicant2Response,
        Applicant2Approved,
        AwaitingPayment,
        AwaitingHWFDecision,
        AwaitingDocuments,
        AwaitingRequestedInformation,
        RequestedInformationSubmitted
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

    public static final EnumSet<State> STATES_NOT_DRAFT_OR_WITHDRAWN_OR_REJECTED = EnumSet.complementOf(EnumSet.of(
        Draft,
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
        AwaitingHWFEvidence,
        AwaitingHWFPartPayment,
        AwaitingDocuments,
        AwaitingRequestedInformation,
        NewPaperCase,
        RequestedInformationSubmitted,
        Submitted,
        Withdrawn,
        Rejected,
        Archived
    ));

    public static final State[] AOS_STATES = {
        Holding, AwaitingConditionalOrder, IssuedToBailiff, AwaitingBailiffService, AwaitingBailiffReferral, BailiffRefused,
        AwaitingServiceConsideration, LAServiceReview, AwaitingServicePayment, AwaitingAlternativeService, AwaitingDwpResponse,
        AwaitingJudgeClarification, PendingServiceAppResponse, GeneralConsiderationComplete, AwaitingGeneralReferralPayment,
        AwaitingGeneralConsideration, GeneralApplicationReceived, PendingHearingOutcome, PendingHearingDate,
        AwaitingGeneralApplicationPayment, AwaitingDocuments, AwaitingGenAppHWFPartPayment, AwaitingGenAppHWFEvidence
    };

    public static final State[] POST_SUBMISSION_PRE_AWAITING_CO_STATES = {
        Submitted,
        AwaitingService,
        AwaitingAos,
        AwaitingServicePayment,
        AwaitingGenAppHWFPartPayment,
        AwaitingGenAppHWFEvidence,
        AwaitingServiceConsideration,
        LAServiceReview,
        AwaitingBailiffReferral,
        BailiffRefused,
        AosOverdue,
        AosDrafted,
        AwaitingBailiffService,
        IssuedToBailiff
    };

    public static final EnumSet<State> STATES_FOR_LINKING_APP2 = EnumSet.complementOf(EnumSet.of(
        Draft,
        Withdrawn,
        Rejected,
        Archived
    ));
}

