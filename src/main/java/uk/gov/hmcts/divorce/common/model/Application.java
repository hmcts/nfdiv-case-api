package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.ServiceMethod.PERSONAL_SERVICE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Application {
    @CCD(ignore = true)
    private static final int SUBMISSION_RESPONSE_DAYS = 14;

    @CCD(
        label = "Has applicant 1's marriage broken down irretrievably?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1ScreenHasMarriageBroken;

    @CCD(
        label = "Has applicant 2's marriage broken down irretrievably?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2ScreenHasMarriageBroken;

    @CCD(
        label = "The applicant has marriage cert?",
        access = {DefaultAccess.class}
    )
    private YesOrNo screenHasMarriageCert;

    @CCD(
        access = {DefaultAccess.class}
    )
    @JsonUnwrapped(prefix = "marriage")
    @Builder.Default
    private MarriageDetails marriageDetails = new MarriageDetails();

    @JsonUnwrapped(prefix = "jurisdiction")
    @Builder.Default
    private Jurisdiction jurisdiction = new Jurisdiction();

    @JsonUnwrapped(prefix = "helpWithFees")
    @CCD(access = {DefaultAccess.class})
    private HelpWithFees helpWithFees;

    @CCD(
        label = "Who is the applicant divorcing?",
        hint = "Husband or Wife?",
        typeOverride = FixedList,
        typeParameterOverride = "WhoDivorcing",
        access = {DefaultAccess.class}
    )
    private WhoDivorcing divorceWho;

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
        label = "Does the applicant want to claim costs?",
        access = {DefaultAccess.class}
    )
    private YesOrNo divorceCostsClaim;

    @CCD(
        label = "The applicant wants/will to apply to have the papers served to the respondent another way.",
        hint = "For example by email, text message or social media. This is a separate application with "
            + "an additional fee, which will need to be reviewed by a judge.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1WantsToHavePapersServedAnotherWay;

    @CCD(
        label = "How would you like the respondent to be served?",
        access = {DefaultAccess.class}
    )
    private ServiceMethod solServiceMethod;

    @CCD(
        label = "I have discussed the possibility of a reconciliation with the applicant.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solStatementOfReconciliationCertify;

    @CCD(
        label = "I have given the applicant the names and addresses of persons qualified to help effect a reconciliation.",
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
        label = "The applicant believes that the facts stated in this application are true.",
        access = {DefaultAccess.class}
    )
    private YesOrNo statementOfTruth;

    @CCD(
        label = "I am duly authorised by the applicant to sign this statement.",
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

    // TODO move to OrderSummary?
    @CCD(
        label = "Solicitor application fee (in pounds)",
        access = {DefaultAccess.class}
    )
    private String solApplicationFeeInPounds;

    @CCD(
        label = "How will payment be made?",
        typeOverride = FixedList,
        typeParameterOverride = "SolicitorPaymentMethod",
        access = {DefaultAccess.class}
    )
    private SolicitorPaymentMethod solPaymentHowToPay;

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
        label = "The respondent agrees that the divorce service can send notifications by email.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2AgreeToReceiveEmails;

    @CCD(
        label = "Is the respondent's email address known?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1KnowsApplicant2EmailAddress;

    @CCD(
        label = "Is the respondent's home address known?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1KnowsApplicant2Address;

    @CCD(
        label = "Are there any existing or previous court proceedings relating to the applicant's marriage, "
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
        label = "Applicant 2 is using digital channel?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app2ContactMethodIsDigital;

    @CCD(
        label = "Cannot upload supporting documents",
        access = {DefaultAccess.class}
    )
    private Set<DocumentType> cannotUploadSupportingDocument;

    @CCD(
        label = "All documents uploaded",
        hint = "Select yes to submit the case, if all documents have been uploaded",
        access = {DefaultAccess.class}
    )
    private YesOrNo documentUploadComplete;

    @CCD(
        label = "Link to online petition",
        access = {DefaultAccess.class}
    )
    private Document miniApplicationLink;

    @CCD(
        label = "Date submitted to HMCTS",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateSubmitted;

    @CCD(
        label = "Is the information provided on the case correct?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2ConfirmApplicant1Information;

    @CCD(
        label = "Explain what is incorrect or needs changing.",
        access = {DefaultAccess.class}
    )
    private String applicant2ExplainsApplicant1IncorrectInformation;

    @JsonIgnore
    public boolean hasStatementOfTruth() {
        return YES.equals(statementOfTruth);
    }

    @JsonIgnore
    public boolean hasSolSignStatementOfTruth() {
        return YES.equals(solSignStatementOfTruth);
    }

    @JsonIgnore
    public LocalDate getDateOfSubmissionResponse() {
        return dateSubmitted == null ? null : dateSubmitted.plusDays(SUBMISSION_RESPONSE_DAYS).toLocalDate();
    }

    public boolean hasAwaitingDocuments() {
        return applicant1WantsToHavePapersServedAnotherWay != null
            && applicant1WantsToHavePapersServedAnotherWay.toBoolean()
            || !isEmpty(cannotUploadSupportingDocument);
    }

    @JsonIgnore
    public boolean isPersonalServiceMethod() {
        return PERSONAL_SERVICE.equals(solServiceMethod);
    }
}
