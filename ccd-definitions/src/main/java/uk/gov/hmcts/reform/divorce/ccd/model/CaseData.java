package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.ccd.sdk.types.CCD;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.ChangedNameHowEnum;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.ConfidentialAddressEnum;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.Gender;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.LegalProcessEnum;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.UseHomeAddressEnum;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.WhoDivorcingEnum;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.types.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.types.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.types.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.types.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.types.FieldType.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode()
public class CaseData {

    @JsonProperty("D8legalProcess")
    @CCD(
        label = "Type",
        hint = "Legal process (divorce, dissolution, judicial separation)",
        type = FixedList,
        typeParameter = "LegalProcessEnum"
    )
    private LegalProcessEnum d8legalProcess;

    @JsonProperty("D8caseReference")
    @CCD(
        label = "FamilyMan reference",
        hint = "FamilyMan case reference"
    )
    private String d8caseReference;

    //TODO: Add type = Label
    @JsonProperty("D8caseReferenceLabel")
    @CCD(
        label = "FamilyMan reference: **${D8caseReference}**",
        hint = "FamilyMan case reference"
    )
    private String d8caseReferenceLabel;

    @JsonProperty("createdDate")
    @CCD(
        label = "Created date",
        hint = "Date case was created",
        type = Date
    )
    private String createdDate;

    @JsonProperty("D8ScreenHasMarriageBroken")
    @CCD(
        label = "Marriage broken irretrievably?",
        hint = "Has petitioner's marriage broken down irretrievably?",
        type = YesOrNo
    )
    private String d8ScreenHasMarriageBroken;

    @JsonProperty("D8ScreenHasRespondentAddress")
    @CCD(
        label = "Address for respondent?",
        hint = "Has the petitioner got a correspondence address for the respondent?",
        type = YesOrNo
    )
    private String d8ScreenHasRespondentAddress;

    @JsonProperty("D8ScreenHasMarriageCert")
    @CCD(
        label = "Petitioner got marriage cert.?",
        hint = "Has the petitioner got their marriage certificate?",
        type = YesOrNo
    )
    private String d8ScreenHasMarriageCert;

    @JsonProperty("D8ScreenHasPrinter")
    @CCD(
        label = "Petitioner access to printer?",
        hint = "Has the petitioner got access to a printer?",
        type = YesOrNo
    )
    private String d8ScreenHasPrinter;

    @JsonProperty("D8DivorceWho")
    @CCD(
        label = "Who is petitioner divorcing?",
        hint = "Husband or Wife?",
        type = FixedList,
        typeParameter = "WhoDivorcingEnum"
    )
    private WhoDivorcingEnum d8DivorceWho;

    @JsonProperty("D8MarriageIsSameSexCouple")
    @CCD(
        label = "Were the petitioner and the respondent a same-sex couple when they got married?",
        type = YesOrNo
    )
    private String d8MarriageIsSameSexCouple;

    @JsonProperty("D8InferredPetitionerGender")
    @CCD(
        label = "What is the petitioner's gender?",
        hint = "The petitioner’s gender is collected for statistical purposes only.",
        type = FixedList,
        typeParameter = "Gender"
    )
    private Gender d8InferredPetitionerGender;

    @JsonProperty("D8InferredRespondentGender")
    @CCD(
        label = "What is the respondent's gender?",
        hint = "The respondent’s gender is collected for statistical purposes only.",
        type = FixedList,
        typeParameter = "Gender"
    )
    private Gender d8InferredRespondentGender;

    @JsonProperty("D8MarriageDate")
    @CCD(
        label = "Marriage date",
        type = Date
    )
    private String d8MarriageDate;

    @JsonProperty("D8MarriedInUk")
    @CCD(
        label = "Did the marriage take place in the UK?",
        type = YesOrNo
    )
    private String d8MarriedInUk;

    @JsonProperty("D8CertificateInEnglish")
    @CCD(
        label = "Marriage certificate in English?",
        type = YesOrNo
    )
    private String d8CertificateInEnglish;

    @JsonProperty("D8CertifiedTranslation")
    @CCD(
        label = "Marriage certificate translation",
        hint = "Is there an English translation of the marriage certificate?",
        type = YesOrNo
    )
    private String d8CertifiedTranslation;

    @JsonProperty("D8MarriagePlaceOfMarriage")
    @CCD(
        label = "Place of marriage",
        hint = "Enter the place of marriage as it appears on the marriage certificate"
    )
    private String d8MarriagePlaceOfMarriage;

    @JsonProperty("D8CountryName")
    @CCD(
        label = "Country of marriage",
        hint = "Enter the country in which the marriage took place"
    )
    private String d8CountryName;

    @JsonProperty("D8MarriagePetitionerName")
    @CCD(
        label = "Petitioner's full name as on marriage certificate",
        hint = "Enter the petitioner's name exactly as it appears on the marriage certificate. Include any extra text such as \"formerly known as\""
    )
    private String d8MarriagePetitionerName;

    @JsonProperty("D8MarriageRespondentName")
    @CCD(
        label = "Respondent's full name as on marriage certificate",
        hint = "Enter the respondent's name exactly as it appears on the marriage certificate. Include any extra text such as \"formerly known as\""
    )
    private String d8MarriageRespondentName;

    @JsonProperty("D8RejectMarriageDetails")
    @CCD(
        label = "Reject marriage details",
        hint = "Rejection reasons for the marriage certificate"
    )
    private RejectReason d8RejectMarriageDetails;

    @JsonProperty("D8PetitionerNameDifferentToMarriageCert")
    @CCD(
        label = "Has the petitioner changed their name since they got married?",
        hint = "Is the petitioner’s current name different to their married name or the name shown on their marriage certificate?",
        type = YesOrNo
    )
    private String d8PetitionerNameDifferentToMarriageCert;

    @JsonProperty("D8PetitionerEmail")
    @CCD(
        label = "Petitioner's email address",
        type = Email
    )
    private String d8PetitionerEmail;

    //TODO: add regular expression "^[0-9 +().-]{9,}$"
    @JsonProperty("D8PetitionerPhoneNumber")
    @CCD(
        label = "Petitioner's phone number"
    )
    private String d8PetitionerPhoneNumber;

    @JsonProperty("D8PetitionerFirstName")
    @CCD(
        label = "First name(s)",
        hint = "Include all middle names here"
    )
    private String d8PetitionerFirstName;

    @JsonProperty("D8PetitionerLastName")
    @CCD(
        label = "Last name"
    )
    private String d8PetitionerLastName;

    @JsonProperty("D8DerivedPetitionerCurrentFullName")
    @CCD(
        label = "Petitioner's current full name",
        hint = "Petitioner's current full name"
    )
    private String d8DerivedPetitionerCurrentFullName;

    @JsonProperty("D8PetitionerHasNameChanged")
    @CCD(
        label = "Did the petitioner change their name?",
        type = YesOrNo
    )
    private String d8PetitionerHasNameChanged;

    @JsonProperty("D8PetitionerNameChangedHow")
    @CCD(
        label = "How did the petitioner change their name?",
        typeParameter = "ChangedNameHowEnum"
    )
    private Set<ChangedNameHowEnum> d8PetitionerNameChangedHow;

    @JsonProperty("D8PetitionerNameChangedHowOtherDetails")
    @CCD(
        label = "Provide details of how they changed their name",
        type = TextArea
    )
    private String d8PetitionerNameChangedHowOtherDetails;

    @JsonProperty("D8PetitionerContactDetailsConfidential")
    @CCD(
        label = "Keep the petitioner's contact details private from the respondent?",
        type = FixedList,
        typeParameter = "ConfidentialAddressEnum"
    )
    private ConfidentialAddressEnum d8PetitionerContactDetailsConfidential;

    @JsonProperty("D8PetitionerHomeAddress")
    @CCD(
        label = "Petitioner's home address",
        hint = "This information may be used to assess jurisdiction or contact the petitioner if you cease to act on behalf of the petitioner"
    )
    private AddressBaseUk d8PetitionerHomeAddress;

    @JsonProperty("D8DerivedPetitionerHomeAddress")
    @CCD(
        label = "The Petitioner's home address",
        type = TextArea
    )
    private String d8DerivedPetitionerHomeAddress;

    @JsonProperty("D8PetitionerCorrespondenceAddress")
    @CCD(
        label = "Petitioner's service address"
    )
    private AddressBaseUk d8PetitionerCorrespondenceAddress;

    @JsonProperty("D8DerivedPetitionerCorrespondenceAddr")
    @CCD(
        label = "Petitioner's service address",
        type = TextArea
    )
    private String d8DerivedPetitionerCorrespondenceAddr;

    @JsonProperty("D8PetitionerCorrespondenceUseHomeAddress")
    @CCD(
        label = "Use home for service address?",
        hint = "Use petitioner's home address as address for service",
        type = YesOrNo
    )
    private String d8PetitionerCorrespondenceUseHomeAddress;

    @JsonProperty("D8RespondentNameAsOnMarriageCertificate")
    @CCD(
        label = "Name changed since marriage?",
        hint = "Is the respondent’s current name different to their married name or the name shown on their marriage certificate?",
        type = YesOrNo
    )
    private String d8RespondentNameAsOnMarriageCertificate;

    @JsonProperty("D8RespondentFirstName")
    @CCD(
        label = "Respondent's First name(s)",
        hint = "Include all middle names here"
    )
    private String d8RespondentFirstName;

    @JsonProperty("D8RespondentLastName")
    @CCD(
        label = "Respondent's Last name"
    )
    private String d8RespondentLastName;

    //TODO: set RegularExpression = ^[0-9 +().-]{9,}$
    @JsonProperty("D8RespondentPhoneNumber")
    @CCD(
        label = "Respondent's phone number"
        // regx = "^[0-9 +().-]{9,}$"
    )
    private String d8RespondentPhoneNumber;

    @JsonProperty("D8DerivedRespondentCurrentName")
    @CCD(
        label = "Respondent's current full name",
        hint = "Respondent's current full name"
    )
    private String d8DerivedRespondentCurrentName;

    @JsonProperty("D8DerivedRespondentSolicitorDetails")
    @CCD(
        label = "Solicitor details",
        type = TextArea
    )
    private String d8DerivedRespondentSolicitorDetails;

    @JsonProperty("D8RespondentHomeAddress")
    @CCD(
        label = "Respondent's home address"
    )
    private AddressBaseUk d8RespondentHomeAddress;

    @JsonProperty("D8RespondentEmailAddress")
    @CCD(
        label = "Respondent Email address written in Petition",
        type = Email
    )
    private String d8RespondentEmailAddress;

    @JsonProperty("D8DerivedRespondentHomeAddress")
    @CCD(
        label = "The Respondent's home address",
        hint = "If the respondent is to be served at their home address, enter the home address here and as the service address below",
        type = TextArea
    )
    private String d8DerivedRespondentHomeAddress;

    @JsonProperty("D8RespondentCorrespondenceAddress")
    @CCD(
        label = "Respondent's service address"
    )
    private AddressBaseUk d8RespondentCorrespondenceAddress;

    @JsonProperty("D8DerivedRespondentCorrespondenceAddr")
    @CCD(
        label = "The Respondent's service address",
        type = TextArea
    )
    private String d8DerivedRespondentCorrespondenceAddr;

    @JsonProperty("D8RespondentSolicitorName")
    @CCD(
        label = "Respondent's solicitor's name"
    )
    private String d8RespondentSolicitorName;

    @JsonProperty("D8RespondentSolicitorCompany")
    @CCD(
        label = "Name of respondent's solicitor's firm"
    )
    private String d8RespondentSolicitorCompany;

    @JsonProperty("D8RespondentCorrespondenceSendToSol")
    @CCD(
        label = "Is the respondent using a solicitor?",
        type = YesOrNo
    )
    private String d8RespondentCorrespondenceSendToSol;

    @JsonProperty("D8RespondentSolicitorReference")
    @CCD(
        label = "Respondent Solicitor Reference"
    )
    private String d8RespondentSolicitorReference;

    @JsonProperty("D8RespondentSolicitorAddress")
    @CCD(
        label = "Respondent's solicitor's address"
    )
    private AddressBaseUk d8RespondentSolicitorAddress;

    //TODO: set RegularExpression = ^[0-9 +().-]{9,}$
    @JsonProperty("D8RespondentSolicitorPhone")
    @CCD(
        label = "Respondent's solicitor's Phone number"
        //regx = "^[0-9 +().-]{9,}$"
    )
    private String d8RespondentSolicitorPhone;

    @JsonProperty("D8RespondentSolicitorEmail")
    @CCD(
        label = "Respondent's solicitor's Email",
        type = Email
    )
    private String d8RespondentSolicitorEmail;

    @JsonProperty("D8DerivedRespondentSolicitorAddr")
    @CCD(
        label = "Respondent's solicitor's firm/ DX address",
        type = TextArea
    )
    private String d8DerivedRespondentSolicitorAddr;

    @JsonProperty("D8RespondentCorrespondenceUseHomeAddress")
    @CCD(
        label = "Serve the respondent at their home address?",
        type = FixedList,
        typeParameter = "UseHomeAddressEnum"
    )
    private UseHomeAddressEnum d8RespondentCorrespondenceUseHomeAddress;

    @JsonProperty("D8RespondentKnowsHomeAddress")
    @CCD(
        label = "Petitioner knows respondent's home address?",
        hint = "Does petitioner know the respondents home address?",
        type = YesOrNo
    )
    private String d8RespondentKnowsHomeAddress;

    //TODO: Label looks incorrect
    @JsonProperty("D8RespondentLivesAtLastAddress")
    @CCD(
        label = "D8RespondentLivesAtLastAddress",
        type = YesOrNo
    )
    private String d8RespondentLivesAtLastAddress;

}
