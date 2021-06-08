package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.model.Payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.IN_PROGRESS;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CaseData {

    @CCD(ignore = true)
    private static final int SUBMISSION_RESPONSE_DAYS = 14;

    @CCD(
        label = "Application type",
        access = {DefaultAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "ApplicationType"
    )
    private ApplicationType applicationType;

    @CCD(
        label = "Divorce or dissolution?",
        access = {DefaultAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "DivorceOrDissolution"
    )
    private DivorceOrDissolution divorceOrDissolution;

    @CCD(
        label = "Has applicant 1's marriage broken down irretrievably?",
        access = {DefaultAccess.class}
    )
    private YesOrNo screenHasMarriageBroken;

    @JsonUnwrapped(prefix = "applicant1")
    @Builder.Default
    private Applicant applicant1 = new Applicant();

    @JsonUnwrapped(prefix = "applicant2")
    @Builder.Default
    private Applicant applicant2 = new Applicant();

    @CCD(
        label = "Help with fees reference",
        regex = "([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$",
        access = {DefaultAccess.class}
    )
    private String helpWithFeesReferenceNumber;

    @CCD(
        label = "Need help with fees?",
        access = {DefaultAccess.class}
    )
    private YesOrNo helpWithFeesNeedHelp;

    @CCD(
        label = "Applicant 1 got marriage cert?",
        access = {DefaultAccess.class}
    )
    private YesOrNo screenHasMarriageCert;

    @CCD(
        label = "Applied for help with fees?",
        access = {DefaultAccess.class}
    )
    private YesOrNo helpWithFeesAppliedForFees;

    @CCD(
        access = {DefaultAccess.class}
    )
    @JsonUnwrapped(prefix = "marriage")
    @Builder.Default
    private MarriageDetails marriageDetails = new MarriageDetails();

    @JsonUnwrapped(prefix = "jurisdiction")
    @Builder.Default
    private Jurisdiction jurisdiction = new Jurisdiction();

    @CCD(
        label = "Who is applicant 1 divorcing?",
        hint = "Husband or Wife?",
        typeOverride = FixedList,
        typeParameterOverride = "WhoDivorcing",
        access = {DefaultAccess.class}
    )
    private WhoDivorcing divorceWho;

    @CCD(
        label = "Applicant 1 Solicitor’s name",
        access = {DefaultAccess.class}
    )
    private String applicant1SolicitorName;

    // TODO applicant1SolicitorReference + Make ApplicantSolicitor
    @CCD(
        label = "Your reference number",
        access = {DefaultAccess.class}
    )
    private String solicitorReference;

    @CCD(
        label = "Applicant 1 Solicitor Phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class}
    )
    private String applicant1SolicitorPhone;

    @CCD(
        label = "Applicant 1 Solicitor Email",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String applicant1SolicitorEmail;

    @CCD(
        label = "Is applicant 1 represented by a solicitor?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1SolicitorRepresented;

    @CCD(
        label = "I confirm I am willing to accept service of all correspondence and orders by email at the email address "
            + "stated above.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solicitorAgreeToReceiveEmails;

    @CCD(
        label = "Firm address/DX address",
        access = {DefaultAccess.class}
    )
    private OrganisationPolicy<UserRole> applicant1OrganisationPolicy;

    @CCD(
        label = "Is this an urgent jurisdiction case?",
        access = {DefaultAccess.class}
    )
    private YesOrNo solUrgentCase;

    @CCD(
        label = "Any supporting information or instructions?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String solUrgentCaseSupportingInformation;

    @CCD(
        label = "Does applicant 1 want to claim costs?",
        access = {DefaultAccess.class}
    )
    private YesOrNo divorceCostsClaim;

    @CCD(
        label = "Does Applicant 1 wish to apply for a financial order?",
        hint = "The court will not start processing your request for a financial order until you submit the separate "
            + "application and pay the fee.",
        access = {DefaultAccess.class}
    )
    private YesOrNo financialOrder;

    @CCD(
        label = "Applicant 1 wants/will to apply to have the papers served to applicant 2 another way.",
        hint = "For example by email, text message or social media. This is a separate application with "
            + "an additional fee, which will need to be reviewed by a judge.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1WantsToHavePapersServedAnotherWay;

    @CCD(
        label = "How would you like applicant 2 to be served?",
        access = {DefaultAccess.class}
    )
    private SolServiceMethod solServiceMethod;

    @CCD(
        label = "I have discussed the possibility of a reconciliation with Applicant 1.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solStatementOfReconciliationCertify;

    @CCD(
        label = "I have given Applicant 1 the names and addresses of persons qualified to help effect a reconciliation.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solStatementOfReconciliationDiscussed;

    @CCD(
        label = "The applicant has given their \"prayer\".",
        hint = "\"The prayer\" means they confirm they wish to dissolve the union, pay any fees (if applicable),"
            + " and have decided how money and property will be split (\"financial order\").",
        access = {DefaultAccess.class}
    )
    private YesOrNo prayerHasBeenGiven;

    @CCD(
        label = "Applicant 1 believes that the facts stated in this application are true.",
        access = {DefaultAccess.class}
    )
    private YesOrNo statementOfTruth;

    @CCD(
        label = "I am duly authorised by Applicant 1 to sign this statement.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solSignStatementOfTruth;

    @CCD(
        label = "Your name",
        access = {DefaultAccess.class}
    )
    private String solStatementOfReconciliationName;

    @CCD(
        label = "Name of your firm",
        access = {DefaultAccess.class}
    )
    private String solStatementOfReconciliationFirm;

    @CCD(
        label = "Additional comments",
        hint = "For the attention of court staff. These comments will not form part of the application",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String statementOfReconciliationComments;

    @CCD(
        label = "Solicitor application fee (in pounds)",
        access = {DefaultAccess.class}
    )
    private String solApplicationFeeInPounds;

    @CCD(
        label = "How will payment be made?",
        typeOverride = FixedList,
        typeParameterOverride = "SolToPay",
        access = {DefaultAccess.class}
    )
    private SolToPay solPaymentHowToPay;

    @CCD(
        label = "Account number",
        access = {DefaultAccess.class}
    )
    private DynamicList pbaNumbers;

    @CCD(
        label = "Fee account reference",
        hint = "This will appear on your statement to help you identify this payment",
        access = {DefaultAccess.class}
    )
    private String feeAccountReference;

    @CCD(
        label = "Here are your order details",
        access = {DefaultAccess.class}
    )
    private OrderSummary applicationFeeOrderSummary;

    @CCD(
        label = "Did you change your last name when you got married?",
        access = {DefaultAccess.class}
    )
    private YesOrNo lastNameChangedWhenMarried;

    @CCD(
        label = "Legal connections",
        hint = "Tick all the reasons that apply:",
        access = {DefaultAccess.class}
    )
    private Set<LegalConnections> legalConnections;


    @CCD(
        label = "Applicant 2's email address",
        access = {DefaultAccess.class}
    )
    private String applicant2EmailAddress;

    @CCD(
        label = "Is applicant 2's email address known?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1KnowsApplicant2EmailAddress;

    @CCD(
        label = "Is applicant 2's home address known?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1KnowsApplicant2Address;

    @CCD(
        label = "Are there any existing or previous court proceedings relating to Applicant 1's marriage, "
            + "property or children?",
        access = {DefaultAccess.class}
    )
    private YesOrNo legalProceedings;

    @CCD(
        label = "What do the legal proceedings relate to?",
        access = {DefaultAccess.class}
    )
    private Set<LegalProceedingsRelated> legalProceedingsRelated;

    @CCD(
        label = "Legal proceeding details",
        hint = "Include the case number(s), if known.",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String legalProceedingsDetails;

    @CCD(
        label = "Claim costs from",
        access = {DefaultAccess.class}
    )
    private Set<ClaimsCostFrom> divorceClaimFrom;

    @CCD(
        label = "Documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> documentsUploaded;

    @CCD(
        label = "Cannot upload supporting documents",
        access = {DefaultAccess.class}
    )
    private Set<DocumentType> cannotUploadSupportingDocument;

    @CCD(
        label = "RDC",
        hint = "Regional divorce unit",
        access = {DefaultAccess.class}
    )
    private Court divorceUnit;

    @CCD(
        label = "Site ID for selected court",
        access = {DefaultAccess.class}
    )
    private String selectedDivorceCentreSiteId;

    @CCD(
        label = "Applicant 2's solicitor's reference",
        access = {DefaultAccess.class}
    )
    private String applicant2SolicitorReference;

    @CCD(
        label = "Documents generated",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> documentsGenerated;

    @CCD(
        label = "Is applicant 2 represented by a solicitor?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2SolicitorRepresented;

    @CCD(
        label = "Applicant 2's solicitor's name",
        access = {DefaultAccess.class}
    )
    private String applicant2SolicitorName;

    @CCD(
        label = "Applicant 2's solicitor's Phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class}
    )
    private String applicant2SolicitorPhone;

    @CCD(
        label = "Applicant 2's solicitor's Email",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String applicant2SolicitorEmail;

    @CCD(
        label = "Applicant 2's solicitor's firm/ DX address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String derivedApplicant2SolicitorAddr;

    @CCD(
        label = "Digital applicant 2 case",
        access = {DefaultAccess.class}
    )
    private YesOrNo app2SolDigital;

    @CCD(
        label = "Applicant 2 is using digital channel?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app2ContactMethodIsDigital;

    @CCD(
        label = "Applicant 2's solicitor's firm address or DX number",
        hint = "Applicant 2 Organisation Details",
        access = {DefaultAccess.class}
    )
    private OrganisationPolicy<UserRole> applicant2OrganisationPolicy;

    @CCD(
        label = "Applicant 2's service address",
        hint = "If applicant 2 is to be served at their home address, enter the home address here and as the service "
            + "address below",
        access = {DefaultAccess.class}
    )
    private AddressGlobalUK applicant2CorrespondenceAddress;

    @CCD(
        label = "Who is the financial order for?",
        access = {DefaultAccess.class}
    )
    private Set<FinancialOrderFor> financialOrderFor;

    @CCD(
        label = "Payments",
        typeOverride = Collection,
        typeParameterOverride = "Payment",
        access = {DefaultAccess.class}
    )
    private List<ListValue<Payment>> payments;

    @CCD(
        label = "Date submitted to HMCTS",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateSubmitted;

    @CCD(
        label = "Case ID for previously Amended Case, which was challenged by applicant 2",
        access = {DefaultAccess.class}
    )
    private CaseLink previousCaseId;

    @CCD(
        label = "Due Date",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @CCD(
        label = "Is HWF Reference OK for the full amount?",
        access = {DefaultAccess.class},
        hint = "Select \"Yes\" if it covers entire amount or \"No\" if doesn't"
    )
    private YesOrNo hwfCodeValidForFullAmount;

    @CCD(
        label = "Is amount different or invalid reference?",
        access = {DefaultAccess.class},
        hint = "Select \"Yes\" for different amount or \"No\" for invalid reference"
    )
    private YesOrNo hwfAmountOutstanding;

    @CCD(
        label = "All documents uploaded",
        hint = "Select yes to submit the case, if all documents have been uploaded",
        access = {DefaultAccess.class}
    )
    private YesOrNo documentUploadComplete;

    @CCD(
        label = "Applicant 2 Invite Access Pin",
        access = {DefaultAccess.class}
    )
    private String invitePin;

    @JsonIgnore
    public LocalDate getDateOfSubmissionResponse() {
        return dateSubmitted == null ? null : dateSubmitted.plusDays(SUBMISSION_RESPONSE_DAYS).toLocalDate();
    }

    @JsonIgnore
    public boolean hasStatementOfTruth() {
        return YES.equals(statementOfTruth);
    }

    @JsonIgnore
    public boolean hasSolSignStatementOfTruth() {
        return YES.equals(solSignStatementOfTruth);
    }

    @JsonIgnore
    public boolean isAmendedCase() {
        return null != previousCaseId;
    }

    @JsonIgnore
    public boolean hasDigitalDetailsForApp2Sol() {
        return YES.equals(app2SolDigital);
    }

    @JsonIgnore
    public boolean hasApplicant2OrgId() {
        if (null != applicant2OrganisationPolicy) {
            String applicant2OrgId = applicant2OrganisationPolicy.getOrganisation().getOrganisationId();
            return !Strings.isNullOrEmpty(applicant2OrgId);
        }
        return false;
    }

    @JsonIgnore
    public boolean hasApplicant1OrgId() {
        if (null != applicant1OrganisationPolicy) {
            String applicant1OrgId = applicant1OrganisationPolicy.getOrganisation().getOrganisationId();
            return !Strings.isNullOrEmpty(applicant1OrgId);
        }
        return false;
    }

    @JsonIgnore
    public Integer getPaymentTotal() {
        return payments == null
            ? 0
            : payments
            .stream()
            .filter(p -> SUCCESS.equals(p.getValue().getPaymentStatus()))
            .map(p -> p.getValue().getPaymentAmount())
            .reduce(0, Integer::sum);
    }

    @JsonIgnore
    public Boolean isLastPaymentInProgress() {
        return payments != null && payments
            .stream()
            .reduce((previous, current) -> current)
            .get()
            .getValue()
            .getPaymentStatus() == IN_PROGRESS;
    }

    @JsonIgnore
    public Boolean wasLastPaymentUnsuccessful() {
        return payments != null && payments
            .stream()
            .reduce((previous, current) -> current)
            .get()
            .getValue()
            .getPaymentStatus() != SUCCESS;
    }

    @JsonIgnore
    public void addToDocumentsGenerated(final ListValue<DivorceDocument> listValue) {

        final List<ListValue<DivorceDocument>> documents = getDocumentsGenerated();

        if (isEmpty(documents)) {
            final List<ListValue<DivorceDocument>> documentList = new ArrayList<>();
            documentList.add(listValue);
            setDocumentsGenerated(documentList);
        } else {
            documents.add(listValue);
        }
    }
}
