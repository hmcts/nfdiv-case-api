package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.andNotShowForState;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildStateTab(configBuilder);
        buildAosTab(configBuilder);
        buildPaymentTab(configBuilder);
        buildLanguageTab(configBuilder);
        buildDocumentsTab(configBuilder);
        buildConfidentialTab(configBuilder);
        buildMarriageCertificateTab(configBuilder);
        buildNotesTab(configBuilder);
        buildGeneralReferralTab(configBuilder);
        buildConfidentialDocumentsTab(configBuilder);
        buildServiceApplicationTab(configBuilder);
        buildConditionalOrderTab(configBuilder);
        buildOutcomeOfConditionalOrderTab(configBuilder);
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
                SUPER_USER, SOLICITOR)
            .showCondition("applicationType=\"soleApplication\" AND "
                + andNotShowForState(Draft, AwaitingHWFDecision, AwaitingPayment, Submitted, AwaitingDocuments))
            .label("LabelAosTabOnlineResponse-Heading", null, "## This is an online AoS response")
            .field("confirmReadPetition")
            .field("jurisdictionAgree")
            .field("jurisdictionDisagreeReason")
            .field("legalProceedingsExist")
            .field("legalProceedingsDescription")
            .field("applicant2UserId")
            .field("dueDate")
            .label("LabelAosTabOnlineResponse-RespondentRepresent", null, "### Respondent")
            .field("applicant2SolicitorRepresented")
            .field("digitalNoticeOfProceedings")
            .field("noticeOfProceedingsEmail")
            .field("noticeOfProceedingsSolicitorFirm");
    }

    private void buildPaymentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field("applicant1HWFReferenceNumber");
    }

    private void buildLanguageTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("languageDetails", "Language")
            .label("LabelLanguageDetails-Applicant", null, "### The applicant")
            .field("applicant1LanguagePreferenceWelsh")
            .label("LabelLanguageDetails-Respondent", null, "### The respondent")
            .field("applicant2LanguagePreferenceWelsh");
    }

    private void buildDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("documents", "Documents")
            .field(CaseData::getDocumentsGenerated)
            .field(CaseData::getApplicant1DocumentsUploaded)
            .field(CaseData::getGeneralOrders)
            .field(CaseData::getDocumentsUploaded)
            .field("certificateOfServiceDocument");
    }

    private void buildConfidentialTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("Confidential", "Confidential Address")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR)
            .showCondition("applicant1KeepContactDetailsConfidential=\"keep\"")
            .field("applicant1CorrespondenceAddress")
            .field("applicant1PhoneNumber")
            .field("applicant1Email")
            .field("applicant1HomeAddress");
    }

    private void buildMarriageCertificateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("marriageDetails", "Marriage Certificate")
            .field("labelContentTheApplicant2UC", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageDate")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCertifyMarriageCertificateIsCorrect")
            .field("marriageMarriageCertificateIsIncorrectDetails", "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
            .field("marriageIssueApplicationWithoutMarriageCertificate", "marriageCertifyMarriageCertificateIsCorrect=\"No\"");
    }

    private void buildNotesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("notes", "Notes")
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
            .field("generalReferralJudgeDetails")
            .field("generalReferralLegalAdvisorDetails")
            .field("generalReferralFeeRequired");
    }

    private void buildConfidentialDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("confidentialDocuments", "Confidential Document")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field(CaseData::getConfidentialDocumentsUploaded);
    }

    private void buildServiceApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("alternativeService", "Service Application")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field("receivedServiceApplicationDate")
            .field("alternativeServiceType")
            .field("receivedServiceAddedDate")
            .field("serviceApplicationDecisionDate")
            .field("deemedServiceDate")
            .field("serviceApplicationGranted")
            .field("dateOfPayment")
            .field("paymentMethod")
            .field("feeAccountNumber", "paymentMethod=\"feePayByAccount\"")
            .field("feeAccountReferenceNumber", "paymentMethod=\"feePayByAccount\"")
            .field("helpWithFeesReferenceNumber", "paymentMethod=\"feePayByHelp\"")
            .field("servicePaymentFeeOrderSummary");
    }

    private void buildConditionalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("conditionalOrder", "Conditional Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, SUPER_USER)
            .showCondition("coDateSubmitted=\"*\"")
            .field("coApplyForConditionalOrder")
            .field("coDateSubmitted")
            .field("coChangeOrAddToApplication")
            .field("LabelConditionalOrderSoT-SoTStatement")
            .field("coSolicitorName")
            .field("coSolicitorFirm")
            .field("coSolicitorAdditionalComments");
    }

    private void buildOutcomeOfConditionalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("outcomeOfConditionalOrder", "Outcome of Conditional Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, SUPER_USER)
            .showCondition("coGranted=\"*\"")
            .field("coDecisionDate")
            .field("coGranted")
            .field("coClaimsGranted")
            .field("coClaimsCostsOrderInformation")
            .field("coWhoPaysCosts")
            .field("coTypeCostsDecision")
            .field("coCostsOrderAdditionalInfo")
            .field("coRefusalDecision")
            .field("coRefusalAdminErrorInfo", "coRefusalDecision=\"adminError\"")
            .field("coRefusalRejectionReason")
            .field("coRefusalRejectionAdditionalInfo", "coRefusalRejectionReason=\"other\"")
            .field("coRefusalClarificationReason")
            .field("coRefusalClarificationAdditionalInfo", "coRefusalClarificationReason=\"other\"")
            .label("labelCoClarificationResponse", null, "## Clarification Response")

            .field("DnClarificationResponse") // "Label": "List of responses for Decree Nisi clarification", "FieldType": "Collection", "FieldTypeParameter": "TextArea",
            .field("DnClarificationUploadDocuments") // "Label": "Upload any other documents per Clarification?", "FieldType": "Collection", "FieldTypeParameter": "Text",
            .label("labelCoPronouncementDetails", null, "## Pronouncement Details")
            .field("BulkListingCaseId") // "Label": "Bulk case reference", "FieldType": "CaseLink",
            .field("CourtName") // "Label": "Court name", "FieldType": "FixedList", "FieldTypeParameter": "DnCourtEnum",
            .field("DateAndTimeOfHearing") // "Label": "Date and time of hearing", "FieldType": "Collection", "FieldTypeParameter": "HearingDateTime",
            .field("PronouncementJudge") // "Label": "Pronouncement Judge", "FieldType": "Text",
            .field("DecreeNisiGrantedDate") // "Label": "Decree Nisi granted date", "FieldType": "Date",
            .field("DAEligibleFromDate") // "Label": "Decree Absolute Eligible From Date", "FieldType": "Date",
            .field("DnOutcomeCase") // "Label": "Case on digital Decree Nisi Outcome", "FieldType": "YesOrNo",
            .label("labelLegalAdvisorDecision", null, "## Legal advisor decision")
            .label("labelJudgeCostsDecision", null, "## Judge costs decision")
            .field("JudgeCostsClaimGranted") // "Label": "Grant Cost Order?", "FieldType": "FixedRadioList", "FieldTypeParameter": "judgeCostsClaimGrantedEnum",
            .field("JudgeWhoPaysCosts") // "Label": "Who should pay?", "FieldType": "FixedRadioList", "FieldTypeParameter": "WhoPaysCostOrderList",
            .field("JudgeTypeCostsDecision") // "Label": "Make a cost order:", "FieldType": "FixedRadioList", "FieldTypeParameter": "CostOrderList",
            .field("JudgeCostsOrderAdditionalInfo"); // "Label": "Additional info", "FieldType": "TextArea",
    }
}
