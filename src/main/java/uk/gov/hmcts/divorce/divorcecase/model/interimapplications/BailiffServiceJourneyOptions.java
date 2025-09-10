package uk.gov.hmcts.divorce.divorcecase.model.interimapplications;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.YesOrNoOrNotKnown;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class BailiffServiceJourneyOptions {
    @CCD(
        label = "Enter your partner's name",
        searchable = false
    )
    private String bailiffPartnersName;

    @CCD(
        label = "Is your partner currently resident in a refuge?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffPartnerInARefuge;

    @CCD(
        label = "Do you know your partner's phone number?",
        searchable = false
    )
    private YesOrNo bailiffKnowPartnersPhone;

    @CCD(
        label = "Partners phone number",
        regex = "^[0-9 +().-]{9,}$",
        searchable = false
    )
    private String bailiffPartnersPhone;

    @CCD(
        label = "Do you know your partner's date of birth?",
        searchable = false
    )
    private YesOrNo bailiffKnowPartnersDateOfBirth;

    @CCD(
        label = "What is your partner's date of birth?",
        typeOverride = Date,
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bailiffPartnersDateOfBirth;

    @CCD(
        label = "What is your partner's approximate age?",
        searchable = false
    )
    private Integer bailiffPartnersApproximateAge;

    @CCD(
        label = "How tall is your partner?",
        searchable = false
    )
    private String bailiffPartnersHeight;

    @CCD(
        label = "What is your partner's hair colour?",
        searchable = false
    )
    private String bailiffPartnersHairColour;

    @CCD(
        label = "What is your partner's eye colour?",
        searchable = false
    )
    private String bailiffPartnersEyeColour;

    @CCD(
        label = "What is your partner's ethnic group?",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffPartnersEthnicGroup;

    @CCD(
        label = "Does your partner have distinguishing features?",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffPartnersDistinguishingFeatures;

    @CCD(
        label = "When is best to serve papers to your partner?",
        searchable = false
    )
    private String bailiffBestTimeToServe;

    @CCD(
        label = "Does your partner have access to a vehicle?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffDoesPartnerHaveVehicle;

    @CCD(
        label = "Vehicle Manufacturer and model",
        searchable = false
    )
    private String bailiffPartnerVehicleModel;

    @CCD(
        label = "Vehicle Colour",
        searchable = false
    )
    private String bailiffPartnerVehicleColour;

    @CCD(
        label = "Vehicle registration number",
        searchable = false
    )
    private String bailiffPartnerVehicleRegistration;

    @CCD(
        label = "Other vehicle details",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffPartnerVehicleOtherDetails;

    @CCD(
        label = "Has your partner been violent in the past?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffHasPartnerBeenViolent;

    @CCD(
        label = "Provide details of any violent incidents",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffPartnerViolenceDetails;

    @CCD(
        label = "Has your partner made threats against you?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffHasPartnerMadeThreats;

    @CCD(
        label = "Provide details of any threats",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffPartnerThreatsDetails;

    @CCD(
        label = "Have police been involved with your partner?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffHavePoliceBeenInvolved;

    @CCD(
        label = "Provide details of any police involvement",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffPoliceInvolvedDetails;

    @CCD(
        label = "Have social services been involved with your partner?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffHaveSocialServicesBeenInvolved;

    @CCD(
        label = "Provide details of any social services involvement",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffSocialServicesInvolvedDetails;

    @CCD(
        label = "Are there any dangerous animals at the property?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffAreThereDangerousAnimals;

    @CCD(
        label = "Provide details of any dangerous animals",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffDangerousAnimalsDetails;

    @CCD(
        label = "Is your partner know to have any mental issues that may affect their behaviour?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffDoesPartnerHaveMentalIssues;

    @CCD(
        label = "Provide details of any mental issues that may affect their behaviour",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffPartnerMentalIssuesDetails;

    @CCD(
        label = "Does your partner hold a firearms license?",
        searchable = false
    )
    private YesOrNoOrNotKnown bailiffDoesPartnerHoldFirearmsLicense;

    @CCD(
        label = "Provide details about the firearms license",
        typeOverride = TextArea,
        searchable = false
    )
    private String bailiffPartnerFirearmsLicenseDetails;
}
