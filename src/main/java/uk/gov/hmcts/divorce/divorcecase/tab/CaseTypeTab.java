package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.notShowForState;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.showForState;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    private static final String IS_SOLE = "applicationType=\"soleApplication\"";
    private static final String IS_JOINT = "applicationType=\"jointApplication\"";
    private static final String IS_JOINT_AND_HWF_ENTERED = "applicationType=\"jointApplication\" AND applicant2HWFReferenceNumber=\"*\"";
    private static final String IS_NEW_PAPER_CASE = "newPaperCase=\"Yes\"";
    private static final String APPLICANT_1_CONTACT_DETAILS_PUBLIC = "applicant1ContactDetailsType!=\"private\"";
    private static final String APPLICANT_1_CONTACT_DETAILS_PRIVATE = "applicant1ContactDetailsType=\"private\"";
    private static final String PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS =
        "paperFormApplicant1NoPaymentIncluded=\"Yes\" AND paperFormSoleOrApplicant1PaymentOther=\"Yes\"";
    private static final String PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS =
        "paperFormApplicant2NoPaymentIncluded=\"Yes\" AND paperFormApplicant2PaymentOther=\"Yes\"";
    private static final String PAPER_FORM_PAYMENT_OTHER_DETAILS =
        String.format("(%s) OR (%s)", PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS, PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS);
    private static final String NEVER_SHOW = "applicationType=\"NEVER_SHOW\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildWarningsTab(configBuilder);
        buildStateTab(configBuilder);
        buildAosTab(configBuilder);
        buildConditionalOrderTab(configBuilder);
        buildOutcomeOfConditionalOrderTab(configBuilder);
        buildFinalOrderTab(configBuilder);
        buildPaymentTab(configBuilder);
        buildDocumentsTab(configBuilder);
        buildNotesTab(configBuilder);
        buildMarriageCertificateTab(configBuilder);
        buildCivilPartnershipCertificateTab(configBuilder);
        buildServiceApplicationTab(configBuilder);
        buildGeneralReferralTab(configBuilder);
        buildLanguageTab(configBuilder);
        buildConfidentialApplicantTab(configBuilder);
        buildConfidentialRespondentTab(configBuilder);
        buildConfidentialApplicant2Tab(configBuilder);
        buildConfidentialDocumentsTab(configBuilder);
        buildCorrespondenceTab(configBuilder);
        buildAmendedApplicationTab(configBuilder);
    }

    private void buildWarningsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("transformationAndOcrWarningsTab", "Warnings")
            .showCondition("warnings!=\"\"")
            .field("warnings");
    }

    private void buildStateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("state", "State")
            .forRoles(APPLICANT_2_SOLICITOR)
            .label("LabelState", null, "#### Case State:  ${[STATE]}");
    }

    //TODO: Need to revisit this tab once the field stated in the ticket NFDIV-595 are available
    private void buildAosTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("aosDetails", "AoS")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR,
                SUPER_USER, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .showCondition("applicationType=\"soleApplication\" AND "
                + notShowForState(
                Draft, AwaitingHWFDecision, AwaitingPayment, Submitted, AwaitingDocuments,
                AwaitingAos, AosDrafted, AosOverdue, AwaitingService))
            .field("applicant2Offline", NEVER_SHOW)
            .label("LabelAosTabOnlineResponse-Heading", "applicant2Offline=\"No\"", "## This is an online AoS response")
            .label("LabelAosTabOfflineResponse-Heading", "applicant2Offline=\"Yes\"", "## This is an offline AoS response")
            .field("confirmReadPetition")
            .field("jurisdictionAgree")
            .field("reasonCourtsOfEnglandAndWalesHaveNoJurisdiction", "jurisdictionAgree=\"No\"")
            .field("inWhichCountryIsYourLifeMainlyBased", "jurisdictionAgree=\"No\"")
            .field("applicant2LegalProceedings")
            .field("applicant2LegalProceedingsDetails")
            .field("dueDate")
            .field("howToRespondApplication")
            .field("applicant2LanguagePreferenceWelsh")
            .field("applicant2SolicitorRepresented")
            .field("applicant2SolicitorEmail","applicant2SolicitorRepresented=\"Yes\"")
            .field("noticeOfProceedingsEmail","applicant2ContactDetailsType!=\"private\" AND applicant2SolicitorRepresented!=\"Yes\"")
            .field("noticeOfProceedingsSolicitorFirm")
            .field("applicant2SolicitorRepresented", NEVER_SHOW)
            .field("statementOfTruth")
            .field("applicant2StatementOfTruth", "statementOfTruth!=\"*\"")
            .field("dateAosSubmitted")
            .field("aosIsDrafted", NEVER_SHOW);
    }

    private void buildPaymentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .label("LabelApplicant1-PaymentHeading", IS_JOINT, "### The applicant")
            .field("applicant2HWFNeedHelp", NEVER_SHOW)
            .field("applicant1HWFReferenceNumber", "applicationType=\"soleApplication\" OR applicant2HWFReferenceNumber=\"*\"")
            .label("LabelApplicant2-PaymentHeading", IS_JOINT_AND_HWF_ENTERED, "### ${labelContentTheApplicant2UC}")
            .field("applicant2HWFReferenceNumber", IS_JOINT_AND_HWF_ENTERED)
            .field("newPaperCase", NEVER_SHOW)
            .label("LabelPaperCase-PaymentHeading", IS_NEW_PAPER_CASE, "### Paper Case Payment")
            .field("paperCasePaymentMethod", IS_NEW_PAPER_CASE)
            .field("paperFormApplicant1NoPaymentIncluded", NEVER_SHOW)
            .field("paperFormApplicant2NoPaymentIncluded", NEVER_SHOW)
            .field("paperFormSoleOrApplicant1PaymentOther", NEVER_SHOW)
            .field("paperFormApplicant2PaymentOther", NEVER_SHOW)
            .label("LabelPaperForm-App1PaymentHeading", PAPER_FORM_PAYMENT_OTHER_DETAILS, "### Paper Form Payment Details")
            .field("paperFormSoleOrApplicant1PaymentOtherDetail", PAPER_FORM_APPLICANT_1_PAYMENT_OTHER_DETAILS)
            .field("paperFormApplicant2PaymentOtherDetail", PAPER_FORM_APPLICANT_2_PAYMENT_OTHER_DETAILS)
            .field("generalApplicationFeeOrderSummary")
            .field("generalApplicationFeePaymentMethod")
            .field("generalApplicationFeeAccountNumber")
            .field("generalApplicationFeeAccountReferenceNumber")
            .field("generalApplicationFeeHelpWithFeesReferenceNumber")
            .field(CaseData::getPaymentHistoryField);
    }

    private void buildLanguageTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("languageDetails", "Language")
            .label("LabelLanguageDetails-Applicant-Sole", IS_SOLE, "### The applicant")
            .label("LabelLanguageDetails-Applicant-Joint", "applicationType=\"jointApplication\"", "### Applicant 1")
            .field("applicant1LanguagePreferenceWelsh")
            .field("applicant1UsedWelshTranslationOnSubmission")
            .field("applicant1LegalProceedingsDetailsTranslated")
            .field("coApplicant1ReasonInformationNotCorrectTranslated")
            .field("applicant1FinalOrderLateExplanationTranslated")
            .label("LabelLanguageDetails-Respondent-Sole", IS_SOLE, "### The respondent")
            .label("LabelLanguageDetails-Respondent-Joint", "applicationType=\"jointApplication\"", "### Applicant 2")
            .field("applicant2LanguagePreferenceWelsh")
            .field("applicant2LegalProceedingsDetailsTranslated")
            .field("applicant2UsedWelshTranslationOnSubmission")
            .field("coApplicant2ReasonInformationNotCorrectTranslated")
            .field("reasonCourtsOfEnglandAndWalesHaveNoJurisdictionTranslated")
            .field("coRefusalClarificationAdditionalInfoTranslated");
    }

    private void buildDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("documents", "Documents")
            .field("documentsGenerated")
            .field("applicant1DocumentsUploaded")
            .field("applicant2DocumentsUploaded")
            .field("scannedDocuments", APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field(CaseData::getGeneralOrders)
            .field("documentsUploaded")
            .field(CaseData::getGeneralEmails)
            .field("certificateOfServiceDocument")
            .field("coCertificateOfEntitlementDocument")
            .field("coProofOfServiceUploadDocuments");
    }

    private void buildCorrespondenceTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("correspondence", "Correspondence")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field(CaseData::getGeneralEmails)
            .field(CaseData::getGeneralLetters);
    }

    private void buildConfidentialApplicantTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("ConfidentialApplicant", "Confidential Applicant")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, APPLICANT_1_SOLICITOR, SUPER_USER)
            .showCondition("applicant1ContactDetailsType=\"private\"")
            .field("applicant1PhoneNumber")
            .field("applicant1Email")
            .field("applicant1Address");
    }

    private void buildConfidentialRespondentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("ConfidentialRespondent", "Confidential Respondent")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, APPLICANT_2_SOLICITOR, SUPER_USER)
            .showCondition("applicant2ContactDetailsType=\"private\" AND applicationType=\"soleApplication\"")
            .field("applicant2PhoneNumber")
            .field("applicant2Email")
            .field("applicant2Address");
    }

    private void buildConfidentialApplicant2Tab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("ConfidentialApplicant2", "Confidential Applicant 2")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, APPLICANT_2_SOLICITOR, SUPER_USER)
            .showCondition("applicant2ContactDetailsType=\"private\" AND applicationType=\"jointApplication\"")
            .field("applicant2PhoneNumber")
            .field("applicant2Email")
            .field("applicant2Address");
    }

    private void buildMarriageCertificateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("marriageDetails", "Marriage Certificate")
            .showCondition("divorceOrDissolution = \"divorce\"")
            .field("labelContentTheApplicant2UC", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("labelContentMarriageOrCivilPartnership", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("labelContentMarriageOrCivilPartnershipUC", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageDate")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\" OR marriagePlaceOfMarriage=\"*\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\" OR marriageCountryOfMarriage=\"*\"")
            .field("marriageCertifyMarriageCertificateIsCorrect")
            .field("marriageMarriageCertificateIsIncorrectDetails", "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
            .field("marriageIssueApplicationWithoutMarriageCertificate", "marriageCertifyMarriageCertificateIsCorrect=\"No\"");
    }

    private void buildCivilPartnershipCertificateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("civilPartnershipDetails", "Civil Partnership Certificate")
            .showCondition("divorceOrDissolution = \"dissolution\"")
            .field("labelContentTheApplicant2UC", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("labelContentMarriageOrCivilPartnership", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("labelContentMarriageOrCivilPartnershipUC", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageDate")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\" OR marriagePlaceOfMarriage=\"*\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\" OR marriageCountryOfMarriage=\"*\"")
            .field("marriageCertifyMarriageCertificateIsCorrect")
            .field("marriageMarriageCertificateIsIncorrectDetails", "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
            .field("marriageIssueApplicationWithoutMarriageCertificate", "marriageCertifyMarriageCertificateIsCorrect=\"No\"");
    }

    private void buildNotesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("notes", "Notes")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field(CaseData::getNotes);
    }

    private void buildGeneralReferralTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("generalReferral", "General Referral")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field("generalReferralReason")
            .field("generalApplicationFrom", "generalApplicationFrom=\"*\"")
            .field("generalApplicationReferralDate", "generalApplicationReferralDate=\"*\"")
            .field("generalApplicationAddedDate")
            .field("generalReferralType")
            .field("alternativeServiceMedium")
            .field("generalReferralJudgeOrLegalAdvisorDetails")
            .field("generalReferralFeeRequired")
            .field("generalReferralFeePaymentMethod")
            .field("generalReferralDecisionDate")
            .field("generalReferralDecision")
            .field("generalReferralDecisionReason")
            .field("generalReferrals");
    }

    private void buildConfidentialDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("confidentialDocuments", "Confidential Document")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field("confidentialDocumentsGenerated")
            .field("confidentialDocumentsUploaded")
            .field("scannedDocuments", APPLICANT_1_CONTACT_DETAILS_PRIVATE);
    }

    private void buildServiceApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("alternativeService", "Service Application")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field("receivedServiceApplicationDate")
            .field("receivedServiceAddedDate")
            .field("alternativeServiceType")
            .field("servicePaymentFeePaymentMethod")
            .field("dateOfPayment", "servicePaymentFeePaymentMethod=\"*\"")
            .field("servicePaymentFeeAccountNumber", "servicePaymentFeePaymentMethod=\"feePayByAccount\"")
            .field("servicePaymentFeeAccountReferenceNumber", "servicePaymentFeePaymentMethod=\"feePayByAccount\"")
            .field("servicePaymentFeeHelpWithFeesReferenceNumber", "servicePaymentFeePaymentMethod=\"feePayByHelp\"")
            .label("bailiffLocalCourtDetailsLabel",
                "localCourtName=\"*\" OR localCourtEmail=\"*\"", "### Bailiff local court details")
            .field("localCourtName")
            .field("localCourtEmail")
            .label("bailiffReturnLabel",
                "certificateOfServiceDate=\"*\" OR successfulServedByBailiff=\"*\" OR reasonFailureToServeByBailiff=\"*\"",
                "### Bailiff return")
            .field("certificateOfServiceDate")
            .label("serviceOutcomeLabel",
                "serviceApplicationGranted=\"No\" OR serviceApplicationGranted=\"Yes\"",
                "### Outcome of Service Application")
            .field("serviceApplicationGranted")
            .field("serviceApplicationDecisionDate")
            .field("serviceApplicationRefusalReason", "serviceApplicationGranted=\"No\"")
            .field("deemedServiceDate")
            .field("successfulServedByBailiff")
            .field("reasonFailureToServeByBailiff")
            .field("alternativeServiceOutcomes");
    }

    private void buildConditionalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("conditionalOrder", "Conditional Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR, SUPER_USER)
            .showCondition("coApplicant1SubmittedDate=\"*\" OR coApplicant2SubmittedDate=\"*\" OR "
                + showForState(ConditionalOrderDrafted, ConditionalOrderPending, AwaitingLegalAdvisorReferral)
            )
            .label("labelConditionalOrderDetails-Applicant1",
                "applicationType=\"jointApplication\" AND coApplicant1ApplyForConditionalOrder=\"*\"",
                "### Applicant 1")
            .label("labelApplicant1-SwitchToSole",
                "finalOrderSwitchedToSole=\"Yes\" AND coApplicant1ApplyForConditionalOrder=\"*\"",
                "### Applicant 1")
            .field("labelContentUnionType", "applicationType=\"NEVER_SHOW\"")
            .field("labelContentDivorceOrCivilPartnershipApplication", "applicationType=\"NEVER_SHOW\"")
            .field("coApplicant1ApplyForConditionalOrder")
            .field("coApplicant1ConfirmInformationStillCorrect")
            .field("coApplicant1ReasonInformationNotCorrect")
            .field("coApplicant1SubmittedDate")
            .field("coApplicant1ChangeOrAddToApplication")
            .field("coApplicant1StatementOfTruth")
            .field("coApplicant1SolicitorName")
            .field("coApplicant1SolicitorFirm")
            .field("coApplicant1SolicitorAdditionalComments")
            .label("labelConditionalOrderDetails-Applicant2",
                "applicationType=\"jointApplication\" AND coApplicant2ApplyForConditionalOrder=\"*\"",
                "### Applicant 2")
            .label("labelApplicant2-SwitchToSole",
                "finalOrderSwitchedToSole=\"Yes\" AND coApplicant2ApplyForConditionalOrder=\"*\"",
                "### Applicant 2")
            .field("coApplicant2ApplyForConditionalOrder")
            .field("coApplicant2ConfirmInformationStillCorrect")
            .field("coApplicant2ReasonInformationNotCorrect")
            .field("coApplicant2SubmittedDate")
            .field("coApplicant2ChangeOrAddToApplication")
            .field("coApplicant2StatementOfTruth")
            .field("coApplicant2SolicitorName")
            .field("coApplicant2SolicitorFirm")
            .field("coApplicant2SolicitorAdditionalComments")
            .field("coScannedD84Form")
            .field("coCourt")
            .field("coDateAndTimeOfHearing")
            .field("coPronouncementJudge")
            .field("coRescindedDate")
            .field("coSwitchedToSole");
    }

    private void buildOutcomeOfConditionalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("outcomeOfConditionalOrder", "Outcome of Conditional Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR, SUPER_USER)
            .showCondition("coGranted=\"*\" OR "
                + showForState(
                    AwaitingAdminClarification,
                    AwaitingClarification,
                    AwaitingAmendedApplication,
                    ClarificationSubmitted)
            )
            .label("labelLegalAdvisorDecision", null, "## Legal advisor decision")
            .field("coDecisionDate")
            .field("coGranted")
            .field("coClaimsGranted")
            .field("coClaimsCostsOrderInformation")
            .field("coLegalAdvisorDecisions")
            .label("labelCoClarificationResponses",
                "coGranted=\"*\" AND coClarificationResponsesSubmitted=\"*\"",
                "## Clarification Responses")
            .field("coClarificationResponsesSubmitted")
            .field("coCannotUploadClarificationDocuments")
            .label("labelCoPronouncementDetails", null, "## Pronouncement Details")
            .field("bulkListCaseReferenceLink")
            .field("coCourt")
            .field("coDateAndTimeOfHearing")
            .field("coPronouncementJudge")
            .field("coGrantedDate")
            .field("dateFinalOrderEligibleFrom")
            .field("coOutcomeCase")
            .label("labelJudgeCostsDecision",
                "coJudgeCostsClaimGranted=\"*\" OR coJudgeCostsOrderAdditionalInfo=\"*\"",
                "## Judge costs decision")
            .field("coJudgeCostsClaimGranted")
            .field("coJudgeCostsOrderAdditionalInfo")
            .field("coCertificateOfEntitlementDocument")
            .field("coConditionalOrderGrantedDocument")
            .field("coRescindedDate");
    }

    private void buildFinalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("finalOrder", "Final Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR, SUPER_USER)
            .showCondition(showForState(
                AwaitingFinalOrder,
                AwaitingJointFinalOrder,
                FinalOrderRequested,
                FinalOrderPending,
                FinalOrderOverdue,
                FinalOrderComplete))
            .field("labelContentFinaliseDivorceOrEndCivilPartnership", "doesApplicant1WantToApplyForFinalOrder=\"NEVER_SHOW\"")
            .field("doesApplicant1WantToApplyForFinalOrder")
            .field("applicant1FinalOrderLateExplanation")
            .field("granted")
            .field("grantedDate")
            .field("dateFinalOrderNoLongerEligible")
            .field("dateFinalOrderEligibleToRespondent", IS_SOLE)
            .field("doesApplicant1IntendToSwitchToSole")
            .field("dateApplicant1DeclaredIntentionToSwitchToSoleFo")
            .field("doesApplicant2IntendToSwitchToSole")
            .field("dateApplicant2DeclaredIntentionToSwitchToSoleFo")
            .field("finalOrderSwitchedToSole")
            .label("labelFinalOrderDetails-SoleRespondent",
                "applicationType=\"soleApplication\" AND doesApplicant2WantToApplyForFinalOrder=\"*\"",
                "### Respondent")
            .field("doesApplicant2WantToApplyForFinalOrder")
            .field("applicant2FinalOrderExplanation");
    }

    private void buildAmendedApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("amendedApplication", "Amended application")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .showCondition("amendedApplications=\"*\"")
            .field("amendedApplications");
    }
}
