package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.divorce.ccd.access.PetitionerOwned;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.ChangedNameHow;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.ConfidentialAddress;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.Gender;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.JurisdictionConnections;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.SolServiceMethod;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.SolToPay;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.WhoDivorcing;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode()
public class CaseData {

    @JsonProperty("divorceOrDissolution")
    @CCD(
        label = "Divorce or Dissolution?",
        access = { PetitionerOwned.class }
    )
    private DivorceOrDissolution divorceOrDissolution;

    @JsonProperty("D8ScreenHasMarriageBroken")
    @CCD(
        label = "Marriage broken irretrievably?",
        hint = "Has petitioner's marriage broken down irretrievably?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8ScreenHasMarriageBroken;

    @JsonProperty("D8MarriageIsSameSexCouple")
    @CCD(
        label = "Were the petitioner and the respondent a same-sex couple when they got married?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8MarriageIsSameSexCouple;

    @JsonProperty("D8InferredPetitionerGender")
    @CCD(
        label = "What is the petitioner's gender?",
        hint = "The petitioner’s gender is collected for statistical purposes only.",
        typeOverride = FixedList,
        typeParameterOverride = "Gender",
        access = { PetitionerOwned.class }
    )
    private Gender d8InferredPetitionerGender;

    @JsonProperty("D8InferredRespondentGender")
    @CCD(
        label = "What is the respondent's gender?",
        hint = "The respondent’s gender is collected for statistical purposes only.",
        typeOverride = FixedList,
        typeParameterOverride = "Gender",
        access = { PetitionerOwned.class }
    )
    private Gender d8InferredRespondentGender;

    @JsonProperty("D8MarriageDate")
    @CCD(
        label = "Marriage date",
        typeOverride = Date,
        access = { PetitionerOwned.class }
    )
    private String d8MarriageDate;

    @JsonProperty("D8HelpWithFeesReferenceNumber")
    @CCD(
        label = "Help with fees reference",
        regex = "([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$",
        access = { PetitionerOwned.class }
    )
    private String d8HelpWithFeesReferenceNumber;

    @JsonProperty("D8HelpWithFeesNeedHelp")
    @CCD(
        label = "Need help with fees?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8HelpWithFeesNeedHelp;

    @JsonProperty("D8ScreenHasMarriageCert")
    @CCD(
        label = "Petitioner got marriage cert.?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8ScreenHasMarriageCert;

    @JsonProperty("D8HelpWithFeesAppliedForFees")
    @CCD(
        label = "Applied for help with fees?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8HelpWithFeesAppliedForFees;

    @JsonProperty("D8MarriedInUk")
    @CCD(
        label = "Did the marriage take place in the UK?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8MarriedInUk;

    @JsonProperty("D8CertificateInEnglish")
    @CCD(
        label = "Marriage certificate in English?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8CertificateInEnglish;

    @JsonProperty("D8CertifiedTranslation")
    @CCD(
        label = "Marriage certificate translation",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8CertifiedTranslation;

    @JsonProperty("D8CountryName")
    @CCD(
        label = "Country where marriage took place",
        access = { PetitionerOwned.class }
    )
    private String d8CountryName;

    @JsonProperty("D8MarriagePlaceOfMarriage")
    @CCD(
        label = "Place of marriage",
        access = { PetitionerOwned.class }
    )
    private String d8MarriagePlaceOfMarriage;

    @JsonProperty("D8PetitionerFirstName")
    @CCD(
        label = "First name(s)",
        hint = "Include all middle names here",
        access = { PetitionerOwned.class }
    )
    private String d8PetitionerFirstName;

    @JsonProperty("D8PetitionerLastName")
    @CCD(
        label = "Last name",
        access = { PetitionerOwned.class }
    )
    private String d8PetitionerLastName;

    @JsonProperty("D8PetitionerEmail")
    @CCD(
        label = "Petitioner's email address",
        typeOverride = Email,
        access = { PetitionerOwned.class }
    )
    private String d8PetitionerEmail;

    @JsonProperty("PetitionerAgreedToReceiveEmails")
    @CCD(
        label = "Petitioner has agreed to receive notifications and be served (delivered) court documents by email",
        access = { PetitionerOwned.class }
    )
    private YesOrNo petitionerAgreedToReceiveEmails;

    @JsonProperty("JurisdictionPetitionerResidence")
    @CCD(
        label = "Is the petitioner resident?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo jurisdictionPetitionerResidence;

    @JsonProperty("JurisdictionRespondentResidence")
    @CCD(
        label = "Is the respondent resident?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo jurisdictionRespondentResidence;

    @JsonProperty("JurisdictionPetitionerDomicile")
    @CCD(
        label = "Is the petitioner domiciled?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo jurisdictionPetitionerDomicile;

    @JsonProperty("JurisdictionRespondentDomicile")
    @CCD(
        label = "Is the respondent domiciled?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo jurisdictionRespondentDomicile;

    @JsonProperty("JurisdictionPetHabituallyResLastTwelveMonths")
    @CCD(
        label = "Has the petitioner been resident for the last twelve months?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo jurisdictionPetHabituallyResLastTwelveMonths;

    @JsonProperty("JurisdictionPetHabituallyResLastSixMonths")
    @CCD(
        label = "Has the petitioner been resident for the last six months?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo jurisdictionPetHabituallyResLastSixMonths;

    @JsonProperty("JurisdictionResidualEligible")
    @CCD(
        label = "Is residual jurisdiction eligible?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo jurisdictionResidualEligible;

    @JsonProperty("JurisdictionBothLastHabituallyResident")
    @CCD(
        label = "Were the both petitioner and respondent last habitually resident, and one still resides?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo jurisdictionBothLastHabituallyResident;

    @JsonProperty("LanguagePreferenceWelsh")
    @CCD(
        label = "What language do you want to receive emails and documents in?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo languagePreferenceWelsh;

    @JsonProperty("D8PetitionerNameDifferentToMarriageCert")
    @CCD(
        label = "Has the petitioner changed their name since they got married?",
        hint = "Is the respondent’s current name different to their married name or the name shown on their "
            + "marriage certificate?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8PetitionerNameDifferentToMarriageCert;

    @JsonProperty("D8PetitionerNameChangedHow")
    @CCD(
        label = "How did the petitioner change their name?",
        access = { PetitionerOwned.class }
    )
    private ChangedNameHow d8PetitionerNameChangedHow;

    @JsonProperty("D8PetitionerNameChangedHowOtherDetails")
    @CCD(
        label = "Provide details of how they changed their name",
        typeOverride = TextArea,
        access = { PetitionerOwned.class }
    )
    private String d8PetitionerNameChangedHowOtherDetails;

    @JsonProperty("D8DivorceWho")
    @CCD(
        label = "Who is petitioner divorcing?",
        hint = "Husband or Wife?",
        typeOverride = FixedList,
        typeParameterOverride = "WhoDivorcing",
        access = { PetitionerOwned.class }
    )
    private WhoDivorcing d8DivorceWho;

    @JsonProperty("D8DerivedPetitionerHomeAddress")
    @CCD(
        label = "The Petitioner's home address",
        typeOverride = TextArea,
        access = { PetitionerOwned.class }
    )
    private String d8DerivedPetitionerHomeAddress;

    @JsonProperty("D8PetitionerPhoneNumber")
    @CCD(
        label = "Petitioner's phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = { PetitionerOwned.class }
    )
    private String d8PetitionerPhoneNumber;

    @JsonProperty("D8PetitionerContactDetailsConfidential")
    @CCD(
        label = "Keep the petitioner's contact details private from the respondent?",
        typeOverride = FixedList,
        typeParameterOverride = "ConfidentialAddress",
        access = { PetitionerOwned.class }
    )
    private ConfidentialAddress d8PetitionerContactDetailsConfidential;

    @JsonProperty("D8RespondentFirstName")
    @CCD(
        label = "Respondent's First name(s)",
        hint = "Include all middle names here",
        access = { PetitionerOwned.class }
    )
    private String d8RespondentFirstName;

    @JsonProperty("D8RespondentLastName")
    @CCD(
        label = "Respondent's Last name",
        access = { PetitionerOwned.class }
    )
    private String d8RespondentLastName;

    @JsonProperty("D8RespondentNameAsOnMarriageCertificate")
    @CCD(
        label = "Name changed since marriage?",
        hint = "Is the respondent’s current name different to their married name or the name shown on their "
            + "marriage certificate?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8RespondentNameAsOnMarriageCertificate;

    @JsonProperty("RespNameDifferentToMarriageCertExplain")
    @CCD(
        label = "Please explain, if known, how their name has changed since they were married.",
        typeOverride = TextArea,
        access = { PetitionerOwned.class }
    )
    private String respNameDifferentToMarriageCertExplain;

    @JsonProperty("PetitionerSolicitorName")
    @CCD(
        label = "Petitioner Solicitor’s name",
        access = { PetitionerOwned.class }
    )
    private String petitionerSolicitorName;

    @JsonProperty("D8SolicitorReference")
    @CCD(
        label = "Your reference number",
        access = { PetitionerOwned.class }
    )
    private String d8SolicitorReference;

    @JsonProperty("PetitionerSolicitorPhone")
    @CCD(
        label = "Petitioner Solicitor Phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = { PetitionerOwned.class }
    )
    private String petitionerSolicitorPhone;

    @JsonProperty("PetitionerSolicitorEmail")
    @CCD(
        label = "Petitioner Solicitor Email",
        typeOverride = Email,
        access = { PetitionerOwned.class }
    )
    private String petitionerSolicitorEmail;

    @JsonProperty("SolicitorAgreeToReceiveEmails")
    @CCD(
        label = "I confirm I am willing to accept service of all correspondence and orders by email at the email address "
            + "stated above.",
        access = { PetitionerOwned.class }
    )
    private YesOrNo solicitorAgreeToReceiveEmails;

    @JsonProperty("DerivedPetitionerSolicitorAddr")
    @CCD(
        label = "Firm address/DX address",
        typeOverride = TextArea,
        access = { PetitionerOwned.class }
    )
    private String derivedPetitionerSolicitorAddr;

    @JsonProperty("PetitionerOrganisationPolicy")
    @CCD(
        label = "Firm address/DX address",
        access = { PetitionerOwned.class }
    )
    private String petitionerOrganisationPolicy;

    @JsonProperty("D8MarriagePetitionerName")
    @CCD(
        label = "Petitioner's full name as on marriage certificate",
        access = { PetitionerOwned.class }
    )
    private String d8MarriagePetitionerName;

    @JsonProperty("D8MarriageRespondentName")
    @CCD(
        label = "Respondent's full name as on marriage certificate",
        access = { PetitionerOwned.class }
    )
    private String d8MarriageRespondentName;

    @JsonProperty("SolUrgentCase")
    @CCD(
        label = "Is this an urgent jurisdiction case?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo solUrgentCase;

    @JsonProperty("SolUrgentCaseSupportingInformation")
    @CCD(
        label = "Any supporting information or instructions?",
        typeOverride = TextArea,
        access = { PetitionerOwned.class }
    )
    private String solUrgentCaseSupportingInformation;

    @JsonProperty("D8DivorceCostsClaim")
    @CCD(
        label = "Does the petitioner want to claim costs?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8DivorceCostsClaim;

    @JsonProperty("D8FinancialOrder")
    @CCD(
        label = "Does the petitioner wish to apply for a financial order?",
        hint = "The court will not start processing your request for a financial order until you submit the separate "
            + "application and pay the fee.",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8FinancialOrder;

    @JsonProperty("SolServiceMethod")
    @CCD(
        label = "How would you like the respondent to be served?",
        access = { PetitionerOwned.class }
    )
    private SolServiceMethod solServiceMethod;

    @JsonProperty("SolStatementOfReconciliationCertify")
    @CCD(
        label = "I have discussed the possibility of a reconciliation with the petitioner.",
        access = { PetitionerOwned.class }
    )
    private YesOrNo solStatementOfReconciliationCertify;

    @JsonProperty("SolStatementOfReconciliationDiscussed")
    @CCD(
        label = "I have given the petitioner the names and addresses of persons qualified to help effect a reconciliation.",
        access = { PetitionerOwned.class }
    )
    private YesOrNo solStatementOfReconciliationDiscussed;

    @JsonProperty("D8StatementOfTruth")
    @CCD(
        label = "The petitioner believes that the facts stated in this application are true.",
        access = { PetitionerOwned.class }
    )
    private YesOrNo d8StatementOfTruth;

    @JsonProperty("SolSignStatementOfTruth")
    @CCD(
        label = "I am duly authorised by the petitioner to sign this statement.",
        access = { PetitionerOwned.class }
    )
    private YesOrNo solSignStatementOfTruth;

    @JsonProperty("SolStatementOfReconciliationName")
    @CCD(
        label = "Your name",
        access = { PetitionerOwned.class }
    )
    private String solStatementOfReconciliationName;

    @JsonProperty("SolStatementOfReconciliationFirm")
    @CCD(
        label = "Name of your firm",
        access = { PetitionerOwned.class }
    )
    private String solStatementOfReconciliationFirm;

    @JsonProperty("StatementOfReconciliationComments")
    @CCD(
        label = "Additional comments",
        hint = "For the attention of court staff. These comments will not form part of the petition",
        typeOverride = TextArea,
        access = { PetitionerOwned.class }
    )
    private String statementOfReconciliationComments;

    @JsonProperty("SolApplicationFeeInPounds")
    @CCD(
        label = "Solicitor application fee (in pounds)",
        access = { PetitionerOwned.class }
    )
    private String solApplicationFeeInPounds;

    @JsonProperty("SolPaymentHowToPay")
    @CCD(
        label = "How will payment be made?",
        typeOverride = FixedList,
        typeParameterOverride = "SolToPay",
        access = { PetitionerOwned.class }
    )
    private SolToPay solPaymentHowToPay;

    @JsonProperty("PbaNumbers")
    @CCD(
        label = "Account number",
        access = { PetitionerOwned.class }
    )
    private DynamicList pbaNumbers;

    @JsonProperty("FeeAccountReference")
    @CCD(
        label = "Enter your reference",
        hint = "This will appear on your statement to help you identify this payment",
        access = { PetitionerOwned.class }
    )
    private String feeAccountReference;

    @JsonProperty("SolApplicationFeeOrderSummary")
    @CCD(
        label = "Here are your order details",
        access = { PetitionerOwned.class }
    )
    private OrderSummary solApplicationFeeOrderSummary;

    @JsonProperty("LastNameChangedWhenMarried")
    @CCD(
        label = "Did you change your last name when you got married?",
        access = { PetitionerOwned.class }
    )
    private YesOrNo lastNameChangedWhenMarried;


    @JsonProperty("JurisdictionConnections")
    @CCD(
        label = "Jurisdiction connections",
        access = { PetitionerOwned.class }
    )
    private Set<JurisdictionConnections> jurisdictionConnections;
}
