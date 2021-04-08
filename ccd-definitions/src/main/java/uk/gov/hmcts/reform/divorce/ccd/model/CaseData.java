package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.divorce.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.ChangedNameHow;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.ConfidentialAddress;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.Gender;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.JurisdictionConnections;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.LegalProceedingsRelated;
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

    @CCD(
        label = "Divorce or Dissolution?",
        access = { DefaultAccess.class }
    )
    private DivorceOrDissolution divorceOrDissolution;

    @CCD(
        label = "Marriage broken irretrievably?",
        hint = "Has petitioner's marriage broken down irretrievably?",
        access = { DefaultAccess.class }
    )
    private YesOrNo screenHasMarriageBroken;

    @CCD(
        label = "Were the petitioner and the respondent a same-sex couple when they got married?",
        access = { DefaultAccess.class }
    )
    private YesOrNo marriageIsSameSexCouple;

    @CCD(
        label = "What is the petitioner's gender?",
        hint = "The petitioner’s gender is collected for statistical purposes only.",
        typeOverride = FixedList,
        typeParameterOverride = "Gender",
        access = { DefaultAccess.class }
    )
    private Gender inferredPetitionerGender;

    @CCD(
        label = "What is the respondent's gender?",
        hint = "The respondent’s gender is collected for statistical purposes only.",
        typeOverride = FixedList,
        typeParameterOverride = "Gender",
        access = { DefaultAccess.class }
    )
    private Gender inferredRespondentGender;

    @CCD(
        label = "Marriage date",
        typeOverride = Date,
        access = { DefaultAccess.class }
    )
    private String marriageDate;

    @CCD(
        label = "Help with fees reference",
        regex = "([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$",
        access = { DefaultAccess.class }
    )
    private String helpWithFeesReferenceNumber;

    @CCD(
        label = "Need help with fees?",
        access = { DefaultAccess.class }
    )
    private YesOrNo helpWithFeesNeedHelp;

    @CCD(
        label = "Petitioner got marriage cert.?",
        access = { DefaultAccess.class }
    )
    private YesOrNo screenHasMarriageCert;

    @CCD(
        label = "Applied for help with fees?",
        access = { DefaultAccess.class }
    )
    private YesOrNo helpWithFeesAppliedForFees;

    @CCD(
        label = "Did the marriage take place in the UK?",
        access = { DefaultAccess.class }
    )
    private YesOrNo marriedInUk;

    @CCD(
        label = "Marriage certificate in English?",
        access = { DefaultAccess.class }
    )
    private YesOrNo certificateInEnglish;

    @CCD(
        label = "Marriage certificate translation",
        access = { DefaultAccess.class }
    )
    private YesOrNo certifiedTranslation;

    @CCD(
        label = "Country where marriage took place",
        access = { DefaultAccess.class }
    )
    private String countryName;

    @CCD(
        label = "Place of marriage",
        access = { DefaultAccess.class }
    )
    private String marriagePlaceOfMarriage;

    @CCD(
        label = "First name(s)",
        hint = "Include all middle names here",
        access = { DefaultAccess.class }
    )
    private String petitionerFirstName;

    @CCD(
        label = "Last name",
        access = { DefaultAccess.class }
    )
    private String petitionerLastName;

    @CCD(
        label = "Petitioner's email address",
        typeOverride = Email,
        access = { DefaultAccess.class }
    )
    private String petitionerEmail;

    @CCD(
        label = "Petitioner has agreed to receive notifications and be served (delivered) court documents by email",
        access = { DefaultAccess.class }
    )
    private YesOrNo petitionerAgreedToReceiveEmails;

    @CCD(
        label = "Is the petitioner resident?",
        access = { DefaultAccess.class }
    )
    private YesOrNo jurisdictionPetitionerResidence;

    @CCD(
        label = "Is the respondent resident?",
        access = { DefaultAccess.class }
    )
    private YesOrNo jurisdictionRespondentResidence;

    @CCD(
        label = "Is the petitioner domiciled?",
        access = { DefaultAccess.class }
    )
    private YesOrNo jurisdictionPetitionerDomicile;

    @CCD(
        label = "Is the respondent domiciled?",
        access = { DefaultAccess.class }
    )
    private YesOrNo jurisdictionRespondentDomicile;

    @CCD(
        label = "Has the petitioner been resident for the last twelve months?",
        access = { DefaultAccess.class }
    )
    private YesOrNo jurisdictionPetHabituallyResLastTwelveMonths;

    @CCD(
        label = "Has the petitioner been resident for the last six months?",
        access = { DefaultAccess.class }
    )
    private YesOrNo jurisdictionPetHabituallyResLastSixMonths;

    @CCD(
        label = "Is residual jurisdiction eligible?",
        access = { DefaultAccess.class }
    )
    private YesOrNo jurisdictionResidualEligible;

    @CCD(
        label = "Were the both petitioner and respondent last habitually resident, and one still resides?",
        access = { DefaultAccess.class }
    )
    private YesOrNo jurisdictionBothLastHabituallyResident;

    @CCD(
        label = "What language do you want to receive emails and documents in?",
        access = { DefaultAccess.class },
        hint = "Select \"No\" for English or \"Yes\" for bilingual"
    )
    private YesOrNo languagePreferenceWelsh;

    @CCD(
        label = "Has the petitioner changed their name since they got married?",
        hint = "Is the respondent’s current name different to their married name or the name shown on their "
            + "marriage certificate?",
        access = { DefaultAccess.class }
    )
    private YesOrNo petitionerNameDifferentToMarriageCertificate;

    @CCD(
        label = "How did the petitioner change their name?",
        access = { DefaultAccess.class }
    )
    private ChangedNameHow petitionerNameChangedHow;

    @CCD(
        label = "Provide details of how they changed their name",
        typeOverride = TextArea,
        access = { DefaultAccess.class }
    )
    private String petitionerNameChangedHowOtherDetails;

    @CCD(
        label = "Who is petitioner divorcing?",
        hint = "Husband or Wife?",
        typeOverride = FixedList,
        typeParameterOverride = "WhoDivorcing",
        access = { DefaultAccess.class }
    )
    private WhoDivorcing divorceWho;

    @CCD(
        label = "The Petitioner's home address",
        typeOverride = TextArea,
        access = { DefaultAccess.class }
    )
    private String derivedPetitionerHomeAddress;

    @CCD(
        label = "Petitioner's phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = { DefaultAccess.class }
    )
    private String petitionerPhoneNumber;

    @CCD(
        label = "Keep the petitioner's contact details private from the respondent?",
        typeOverride = FixedList,
        typeParameterOverride = "ConfidentialAddress",
        access = { DefaultAccess.class }
    )
    private ConfidentialAddress petitionerContactDetailsConfidential;

    @CCD(
        label = "Respondent's First name(s)",
        hint = "Include all middle names here",
        access = { DefaultAccess.class }
    )
    private String respondentFirstName;

    @CCD(
        label = "Respondent's Last name",
        access = { DefaultAccess.class }
    )
    private String respondentLastName;

    @CCD(
        label = "Name changed since marriage?",
        hint = "Is the respondent’s current name different to their married name or the name shown on their "
            + "marriage certificate?",
        access = { DefaultAccess.class }
    )
    private YesOrNo respondentNameAsOnMarriageCertificate;

    @CCD(
        label = "Please explain, if known, how their name has changed since they were married.",
        typeOverride = TextArea,
        access = { DefaultAccess.class }
    )
    private String respNameDifferentToMarriageCertExplain;

    @CCD(
        label = "Petitioner Solicitor’s name",
        access = { DefaultAccess.class }
    )
    private String petitionerSolicitorName;

    @CCD(
        label = "Your reference number",
        access = { DefaultAccess.class }
    )
    private String solicitorReference;

    @CCD(
        label = "Petitioner Solicitor Phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = { DefaultAccess.class }
    )
    private String petitionerSolicitorPhone;

    @CCD(
        label = "Petitioner Solicitor Email",
        typeOverride = Email,
        access = { DefaultAccess.class }
    )
    private String petitionerSolicitorEmail;

    @CCD(
        label = "I confirm I am willing to accept service of all correspondence and orders by email at the email address "
            + "stated above.",
        access = { DefaultAccess.class }
    )
    private YesOrNo solicitorAgreeToReceiveEmails;

    @CCD(
        label = "Firm address/DX address",
        typeOverride = TextArea,
        access = { DefaultAccess.class }
    )
    private String derivedPetitionerSolicitorAddress;

    @CCD(
        label = "Firm address/DX address",
        access = { DefaultAccess.class }
    )
    private String petitionerOrganisationPolicy;

    @CCD(
        label = "Petitioner's full name as on marriage certificate",
        access = { DefaultAccess.class }
    )
    private String marriagePetitionerName;

    @CCD(
        label = "Respondent's full name as on marriage certificate",
        access = { DefaultAccess.class }
    )
    private String marriageRespondentName;

    @CCD(
        label = "Is this an urgent jurisdiction case?",
        access = { DefaultAccess.class }
    )
    private YesOrNo solUrgentCase;

    @CCD(
        label = "Any supporting information or instructions?",
        typeOverride = TextArea,
        access = { DefaultAccess.class }
    )
    private String solUrgentCaseSupportingInformation;

    @CCD(
        label = "Does the petitioner want to claim costs?",
        access = { DefaultAccess.class }
    )
    private YesOrNo divorceCostsClaim;

    @CCD(
        label = "Does the petitioner wish to apply for a financial order?",
        hint = "The court will not start processing your request for a financial order until you submit the separate "
            + "application and pay the fee.",
        access = { DefaultAccess.class }
    )
    private YesOrNo financialOrder;

    @CCD(
        label = "How would you like the respondent to be served?",
        access = { DefaultAccess.class }
    )
    private SolServiceMethod solServiceMethod;

    @CCD(
        label = "I have discussed the possibility of a reconciliation with the petitioner.",
        access = { DefaultAccess.class }
    )
    private YesOrNo solStatementOfReconciliationCertify;

    @CCD(
        label = "I have given the petitioner the names and addresses of persons qualified to help effect a reconciliation.",
        access = { DefaultAccess.class }
    )
    private YesOrNo solStatementOfReconciliationDiscussed;

    @CCD(
        label = "The petitioner believes that the facts stated in this application are true.",
        access = { DefaultAccess.class }
    )
    private YesOrNo statementOfTruth;

    @CCD(
        label = "I am duly authorised by the petitioner to sign this statement.",
        access = { DefaultAccess.class }
    )
    private YesOrNo solSignStatementOfTruth;

    @CCD(
        label = "Your name",
        access = { DefaultAccess.class }
    )
    private String solStatementOfReconciliationName;

    @CCD(
        label = "Name of your firm",
        access = { DefaultAccess.class }
    )
    private String solStatementOfReconciliationFirm;

    @CCD(
        label = "Additional comments",
        hint = "For the attention of court staff. These comments will not form part of the petition",
        typeOverride = TextArea,
        access = { DefaultAccess.class }
    )
    private String statementOfReconciliationComments;

    @CCD(
        label = "Solicitor application fee (in pounds)",
        access = { DefaultAccess.class }
    )
    private String solApplicationFeeInPounds;

    @CCD(
        label = "How will payment be made?",
        typeOverride = FixedList,
        typeParameterOverride = "SolToPay",
        access = { DefaultAccess.class }
    )
    private SolToPay solPaymentHowToPay;

    @CCD(
        label = "Account number",
        access = { DefaultAccess.class }
    )
    private DynamicList pbaNumbers;

    @CCD(
        label = "Enter your reference",
        hint = "This will appear on your statement to help you identify this payment",
        access = { DefaultAccess.class }
    )
    private String feeAccountReference;

    @CCD(
        label = "Here are your order details",
        access = { DefaultAccess.class }
    )
    private OrderSummary solApplicationFeeOrderSummary;

    @CCD(
        label = "Did you change your last name when you got married?",
        access = { DefaultAccess.class }
    )
    private YesOrNo lastNameChangedWhenMarried;

    @CCD(
        label = "Jurisdiction connections",
        access = { DefaultAccess.class }
    )
    private Set<JurisdictionConnections> jurisdictionConnections;

    @CCD(
        label = "Your partner's email address",
        access = { DefaultAccess.class }
    )
    private String respondentEmailAddress;

    @CCD(
        label = "Do you know their email address",
        access = { DefaultAccess.class }
    )
    private YesOrNo petitionerKnowsRespondentsEmailAddress;

    @CCD(
        label = "Do you know their address",
        access = { DefaultAccess.class }
    )
    private YesOrNo petitionerKnowsRespondentsAddress;

    @CCD(
        label = "Any other legal proceedings relating to your civil partnership, property or children?",
        access = { DefaultAccess.class }
    )
    private YesOrNo legalProceedings;

    @CCD(
        label = "What do the legal proceedings relate to?",
        access = { DefaultAccess.class }
    )
    private LegalProceedingsRelated legalProceedingsRelated;
}
