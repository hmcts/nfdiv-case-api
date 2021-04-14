package uk.gov.hmcts.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.ccd.access.DefaultAccess;
import uk.gov.hmcts.divorce.ccd.model.enums.ChangedNameHow;
import uk.gov.hmcts.divorce.ccd.model.enums.ClaimsCostFrom;
import uk.gov.hmcts.divorce.ccd.model.enums.ConfidentialAddress;
import uk.gov.hmcts.divorce.ccd.model.enums.Court;
import uk.gov.hmcts.divorce.ccd.model.enums.DivorceOrDissolution;
import uk.gov.hmcts.divorce.ccd.model.enums.FinancialOrderFor;
import uk.gov.hmcts.divorce.ccd.model.enums.Gender;
import uk.gov.hmcts.divorce.ccd.model.enums.JurisdictionConnections;
import uk.gov.hmcts.divorce.ccd.model.enums.LegalProceedingsRelated;
import uk.gov.hmcts.divorce.ccd.model.enums.SolServiceMethod;
import uk.gov.hmcts.divorce.ccd.model.enums.SolToPay;
import uk.gov.hmcts.divorce.ccd.model.enums.WhoDivorcing;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CaseData {

    @CCD(
        label = "Divorce or Dissolution?",
        access = {DefaultAccess.class}
    )
    private DivorceOrDissolution divorceOrDissolution;

    @CCD(
        label = "Marriage broken irretrievably?",
        hint = "Has applicant's marriage broken down irretrievably?",
        access = {DefaultAccess.class}
    )
    private YesOrNo screenHasMarriageBroken;

    @CCD(
        label = "Were the applicant and the co-applicant a same-sex couple when they got married?",
        access = {DefaultAccess.class}
    )
    private YesOrNo marriageIsSameSexCouple;

    @CCD(
        label = "What is the applicant's gender?",
        hint = "The applicant’s gender is collected for statistical purposes only.",
        typeOverride = FixedList,
        typeParameterOverride = "Gender",
        access = {DefaultAccess.class}
    )
    private Gender inferredApplicantGender;

    @CCD(
        label = "What is the co-applicant's gender?",
        hint = "The co-applicant’s gender is collected for statistical purposes only.",
        typeOverride = FixedList,
        typeParameterOverride = "Gender",
        access = {DefaultAccess.class}
    )
    private Gender inferredCoApplicantGender;

    @CCD(
        label = "Marriage date",
        typeOverride = Date,
        access = {DefaultAccess.class}
    )
    private String marriageDate;

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
        label = "Applicant got marriage cert.?",
        access = {DefaultAccess.class}
    )
    private YesOrNo screenHasMarriageCert;

    @CCD(
        label = "Applied for help with fees?",
        access = {DefaultAccess.class}
    )
    private YesOrNo helpWithFeesAppliedForFees;

    @CCD(
        label = "Did the marriage take place in the UK?",
        access = {DefaultAccess.class}
    )
    private YesOrNo marriedInUk;

    @CCD(
        label = "Marriage certificate in English?",
        access = {DefaultAccess.class}
    )
    private YesOrNo certificateInEnglish;

    @CCD(
        label = "Marriage certificate translation",
        access = {DefaultAccess.class}
    )
    private YesOrNo certifiedTranslation;

    @CCD(
        label = "Country of marriage",
        hint = "Enter the country in which the marriage took place",
        access = {DefaultAccess.class}
    )
    private String countryName;

    @CCD(
        label = "Place of marriage",
        hint = "Enter the place of marriage as it appears on the marriage certificate",
        access = {DefaultAccess.class}
    )
    private String marriagePlaceOfMarriage;

    @CCD(
        label = "First name(s)",
        hint = "Include all middle names here",
        access = {DefaultAccess.class}
    )
    private String applicantFirstName;

    @CCD(
        label = "Last name",
        access = {DefaultAccess.class}
    )
    private String applicantLastName;

    @CCD(
        label = "Applicant's email address",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String applicantEmail;

    @CCD(
        label = "Applicant has agreed to receive notifications and be served (delivered) court documents by email",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantAgreedToReceiveEmails;

    @CCD(
        label = "Is the applicant resident?",
        access = {DefaultAccess.class}
    )
    private YesOrNo jurisdictionApplicantResidence;

    @CCD(
        label = "Is the co-applicant resident?",
        access = {DefaultAccess.class}
    )
    private YesOrNo jurisdictionCoApplicantResidence;

    @CCD(
        label = "Is the applicant domiciled?",
        access = {DefaultAccess.class}
    )
    private YesOrNo jurisdictionApplicantDomicile;

    @CCD(
        label = "Is the co-applicant domiciled?",
        access = {DefaultAccess.class}
    )
    private YesOrNo jurisdictionCoApplicantDomicile;

    @CCD(
        label = "Has the applicant been resident for the last twelve months?",
        access = {DefaultAccess.class}
    )
    private YesOrNo jurisdictionPetHabituallyResLastTwelveMonths;

    @CCD(
        label = "Has the applicant been resident for the last six months?",
        access = {DefaultAccess.class}
    )
    private YesOrNo jurisdictionPetHabituallyResLastSixMonths;

    @CCD(
        label = "Is residual jurisdiction eligible?",
        access = {DefaultAccess.class}
    )
    private YesOrNo jurisdictionResidualEligible;

    @CCD(
        label = "Were the both applicant and co-applicant last habitually resident, and one still resides?",
        access = {DefaultAccess.class}
    )
    private YesOrNo jurisdictionBothLastHabituallyResident;

    @CCD(
        label = "What language do you want to receive emails and documents in?",
        access = {DefaultAccess.class},
        hint = "Select \"No\" for English or \"Yes\" for bilingual"
    )
    private YesOrNo languagePreferenceWelsh;

    @CCD(
        label = "Has the applicant changed their name since they got married?",
        hint = "Is the co-applicant’s current name different to their married name or the name shown on their "
            + "marriage certificate?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantNameDifferentToMarriageCertificate;

    @CCD(
        label = "How did the applicant change their name?",
        access = {DefaultAccess.class}
    )
    private ChangedNameHow applicantNameChangedHow;

    @CCD(
        label = "Provide details of how they changed their name",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String applicantNameChangedHowOtherDetails;

    @CCD(
        label = "Who is applicant divorcing?",
        hint = "Husband or Wife?",
        typeOverride = FixedList,
        typeParameterOverride = "WhoDivorcing",
        access = {DefaultAccess.class}
    )
    private WhoDivorcing divorceWho;

    @CCD(
        label = "The Applicant's home address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String derivedApplicantHomeAddress;

    @CCD(
        label = "Is applicant home address an international address?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantHomeAddressIsInternational;

    @CCD(
        label = "Applicant's phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class}
    )
    private String applicantPhoneNumber;

    @CCD(
        label = "Keep the applicant's contact details private from the co-applicant?",
        typeOverride = FixedList,
        typeParameterOverride = "ConfidentialAddress",
        access = {DefaultAccess.class}
    )
    private ConfidentialAddress applicantContactDetailsConfidential;

    @CCD(
        label = "Co-Applicant's First name(s)",
        hint = "Include all middle names here",
        access = {DefaultAccess.class}
    )
    private String coApplicantFirstName;

    @CCD(
        label = "Co-Applicant's Last name",
        access = {DefaultAccess.class}
    )
    private String coApplicantLastName;

    @CCD(
        label = "Name changed since marriage?",
        hint = "Is the co-applicant’s current name different to their married name or the name shown on their "
            + "marriage certificate?",
        access = {DefaultAccess.class}
    )
    private YesOrNo coApplicantNameAsOnMarriageCertificate;

    @CCD(
        label = "Please explain, if known, how their name has changed since they were married.",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String coApplicantNameDifferentToMarriageCertExplain;

    @CCD(
        label = "Applicant Solicitor’s name",
        access = {DefaultAccess.class}
    )
    private String applicantSolicitorName;

    @CCD(
        label = "Your reference number",
        access = {DefaultAccess.class}
    )
    private String solicitorReference;

    @CCD(
        label = "Applicant Solicitor Phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class}
    )
    private String applicantSolicitorPhone;

    @CCD(
        label = "Applicant Solicitor Email",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String applicantSolicitorEmail;

    @CCD(
        label = "I confirm I am willing to accept service of all correspondence and orders by email at the email address "
            + "stated above.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solicitorAgreeToReceiveEmails;

    @CCD(
        label = "Firm address/DX address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String derivedApplicantSolicitorAddress;

    @CCD(
        label = "Firm address/DX address",
        access = {DefaultAccess.class}
    )
    private OrganisationPolicy<UserRole> applicantOrganisationPolicy;

    @CCD(
        label = "Applicant's full name as on marriage certificate",
        hint = "Enter the applicant's name exactly as it appears on the marriage certificate. "
            + " Include any extra text such as \"formerly known as\"",
        access = {DefaultAccess.class}
    )
    private String marriageApplicantName;

    @CCD(
        label = "Co-Applicant's full name as on marriage certificate",
        hint = "Enter the co-applicant's name exactly as it appears on the marriage certificate. "
            + " Include any extra text such as \"formerly known as\"",
        access = {DefaultAccess.class}
    )
    private String marriageCoApplicantName;

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
        label = "Does the applicant wish to apply for a financial order?",
        hint = "The court will not start processing your request for a financial order until you submit the separate "
            + "application and pay the fee.",
        access = {DefaultAccess.class}
    )
    private YesOrNo financialOrder;

    @CCD(
        label = "Applicant wants/will to apply to have the papers served to the co-applicant another way.",
        hint = "For example by email, text message or social media. This is a separate application with "
            + "an additional fee, which will need to be reviewed by a judge.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantWantsToHavePapersServedAnotherWay;

    @CCD(
        label = "How would you like the co-applicant to be served?",
        access = {DefaultAccess.class}
    )
    private SolServiceMethod solServiceMethod;

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
        hint = "For the attention of court staff. These comments will not form part of the petition",
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
        label = "Enter your reference",
        hint = "This will appear on your statement to help you identify this payment",
        access = {DefaultAccess.class}
    )
    private String feeAccountReference;

    @CCD(
        label = "Here are your order details",
        access = {DefaultAccess.class}
    )
    private OrderSummary solApplicationFeeOrderSummary;

    @CCD(
        label = "Did you change your last name when you got married?",
        access = {DefaultAccess.class}
    )
    private YesOrNo lastNameChangedWhenMarried;

    @CCD(
        label = "Jurisdiction connections",
        access = {DefaultAccess.class}
    )
    private Set<JurisdictionConnections> jurisdictionConnections;

    @CCD(
        label = "Your partner's email address",
        access = {DefaultAccess.class}
    )
    private String coApplicantEmailAddress;

    @CCD(
        label = "Do you know their email address",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantKnowsCoApplicantsEmailAddress;

    @CCD(
        label = "Do you know their address",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantKnowsCoApplicantsAddress;

    @CCD(
        label = "The Co-Applicant's home address",
        hint = "If the co-applicant is to be served at their home address, enter the home address here and as the service "
            + "address below",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String derivedCoApplicantHomeAddress;

    @CCD(
        label = "Is co-applicant's home address an international address?",
        access = {DefaultAccess.class}
    )
    private YesOrNo coApplicantHomeAddressIsInternational;

    @CCD(
        label = "Any there any existing or previous court proceedings relating to the applicant's marriage, "
            + "property or children?",
        access = {DefaultAccess.class}
    )
    private YesOrNo legalProceedings;

    @CCD(
        label = "Legal proceeding details",
        hint = "Include the case number(s), if known.",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String legalProceedingsDetails;


    @CCD(
        label = "What do the legal proceedings relate to?",
        access = {DefaultAccess.class}
    )
    private Set<LegalProceedingsRelated> legalProceedingsRelated;

    @CCD(
        label = "Claim costs from",
        access = {DefaultAccess.class}
    )
    private Set<ClaimsCostFrom> divorceClaimFrom;

    @CCD(
        label = "Created date",
        hint = "Date case was created",
        access = {DefaultAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

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
        label = "Co-Applicant's solicitor's reference",
        access = {DefaultAccess.class}
    )
    private String coApplicantSolicitorReference;

    @CCD(
        label = "Documents generated",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<DivorceDocument> documentsGenerated;

    @CCD(
        label = "Is the co-applicant represented by a solicitor?",
        access = {DefaultAccess.class}
    )
    private YesOrNo coApplicantSolicitorRepresented;

    @CCD(
        label = "Co-Applicant's solicitor's name",
        access = {DefaultAccess.class}
    )
    private String coApplicantSolicitorName;

    @CCD(
        label = "Co-Applicant's solicitor's Phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class}
    )
    private String coApplicantSolicitorPhone;

    @CCD(
        label = "Co-Applicant's solicitor's Email",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String coApplicantSolicitorEmail;

    @CCD(
        label = "Co-Applicant's solicitor's firm/ DX address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String derivedCoApplicantSolicitorAddr;

    @CCD(
        label = "Digital co-applicant case",
        access = {DefaultAccess.class}
    )
    private YesOrNo coApplicantSolDigital;

    @CCD(
        label = "Co-Applicant solicitor's firm",
        hint = "Co-Applicant Organisation Details",
        access = {DefaultAccess.class}
    )
    private OrganisationPolicy<UserRole> coApplicantOrganisationPolicy;

    @CCD(
        label = "The Co-Applicant's service address",
        hint = "If the co-applicant is to be served at their home address, enter the home address here and as the service "
            + "address below",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String derivedCoApplicantCorrespondenceAddr;

    @CCD(
        label = "Who is the financial order for?",
        access = {DefaultAccess.class}
    )
    private Set<FinancialOrderFor> financialOrderFor;
}
