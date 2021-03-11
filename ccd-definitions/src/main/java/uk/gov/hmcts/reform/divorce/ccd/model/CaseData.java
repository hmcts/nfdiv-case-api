package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.Gender;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode()
public class CaseData {

    @JsonProperty("divorceOrDissolution")
    @CCD(
        label = "Divorce or Dissolution?"
    )
    private DivorceOrDissolution divorceOrDissolution;

    @JsonProperty("D8ScreenHasMarriageBroken")
    @CCD(
        label = "Marriage broken irretrievably?",
        hint = "Has petitioner's marriage broken down irretrievably?"
    )
    private YesOrNo d8ScreenHasMarriageBroken;

    @JsonProperty("D8MarriageIsSameSexCouple")
    @CCD(
        label = "Were the petitioner and the respondent a same-sex couple when they got married?"
    )
    private YesOrNo d8MarriageIsSameSexCouple;

    @JsonProperty("D8InferredPetitionerGender")
    @CCD(
        label = "What is the petitioner's gender?",
        hint = "The petitioner’s gender is collected for statistical purposes only."
    )
    private Gender d8InferredPetitionerGender;

    @JsonProperty("D8InferredRespondentGender")
    @CCD(
        label = "What is the respondent's gender?",
        hint = "The respondent’s gender is collected for statistical purposes only."
    )
    private Gender d8InferredRespondentGender;

    @JsonProperty("D8MarriageDate")
    @CCD(
        label = "Marriage date",
        type = Date
    )
    private String d8MarriageDate;

    @JsonProperty("D8HelpWithFeesReferenceNumber")
    @CCD(
        label = "Help with fees reference",
        regex = "([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$"
    )
    private String d8HelpWithFeesReferenceNumber;

    @JsonProperty("D8HelpWithFeesNeedHelp")
    @CCD(
        label = "Need help with fees?"
    )
    private YesOrNo d8HelpWithFeesNeedHelp;

    @JsonProperty("D8ScreenHasMarriageCert")
    @CCD(
        label = "Petitioner got marriage cert.?"
    )
    private YesOrNo d8ScreenHasMarriageCert;

    @JsonProperty("D8HelpWithFeesAppliedForFees")
    @CCD(
        label = "Applied for help with fees?"
    )
    private YesOrNo d8HelpWithFeesAppliedForFees;

    @JsonProperty("D8MarriedInUk")
    @CCD(
        label = "Did the marriage take place in the UK?"
    )
    private YesOrNo d8MarriedInUk;

    @JsonProperty("D8CertificateInEnglish")
    @CCD(
        label = "Marriage certificate in English?"
    )
    private YesOrNo d8CertificateInEnglish;

    @JsonProperty("D8CertifiedTranslation")
    @CCD(
        label = "Marriage certificate translation"
    )
    private YesOrNo d8CertifiedTranslation;
    
    @JsonProperty("D8CountryName")
    @CCD(
        label = "Country where marriage took place"
    )
    private String d8CountryName;
    
    @JsonProperty("D8MarriagePlaceOfMarriage")
    @CCD(
        label = "Place of marriage"
    )
    private String d8MarriagePlaceOfMarriage;

    @JsonProperty("D8PetitionerFirstName")
    @CCD(
        label = "First name(s)",
        hint = "Include all middle names here")
    private String d8PetitionerFirstName;

    @JsonProperty("D8PetitionerLastName")
    @CCD(
        label = "Last name"
    )
    private String d8PetitionerLastName;

    @JsonProperty("D8PetitionerEmail")
    @CCD(
        label = "Petitioner's email address",
        type = Email
    )
    private String d8PetitionerEmail;

    @JsonProperty("JurisdictionPetitionerResidence")
    @CCD(
        label = "Is the petitioner resident?"
    )
    private YesOrNo jurisdictionPetitionerResidence;

    @JsonProperty("JurisdictionRespondentResidence")
    @CCD(
        label = "Is the respondent resident?"
    )
    private YesOrNo jurisdictionRespondentResidence;

    @JsonProperty("JurisdictionPetitionerDomicile")
    @CCD(
        label = "Is the petitioner domiciled?"
    )
    private YesOrNo jurisdictionPetitionerDomicile;

    @JsonProperty("JurisdictionRespondentDomicile")
    @CCD(
        label = "Is the respondent domiciled?"
    )
    private YesOrNo jurisdictionRespondentDomicile;

    @JsonProperty("JurisdictionPetHabituallyResLastTwelveMonths")
    @CCD(
        label = "Has the petitioner been resident for the last twelve months?"
    )
    private YesOrNo jurisdictionPetHabituallyResLastTwelveMonths;

    @JsonProperty("JurisdictionPetHabituallyResLastSixMonths")
    @CCD(
        label = "Has the petitioner been resident for the last six months?"
    )
    private YesOrNo jurisdictionPetHabituallyResLastSixMonths;

    @JsonProperty("JurisdictionResidualEligible")
    @CCD(
        label = "Is residual jurisdiction eligible?"
    )
    private YesOrNo jurisdictionResidualEligible;

    @JsonProperty("JurisdictionBothLastHabituallyResident")
    @CCD(
        label = "Were the both petitioner and respondent last habitually resident, and one still resides?"
    )
    private YesOrNo jurisdictionBothLastHabituallyResident;

}
