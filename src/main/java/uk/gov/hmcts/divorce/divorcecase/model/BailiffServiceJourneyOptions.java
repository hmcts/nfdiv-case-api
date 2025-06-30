package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BailiffServiceJourneyOptions {
    @CCD(label = "Enter your partner's name")
    private String bailiffPartnersName;

    @CCD(label = "Is your partner currently resident in a refuge?")
    private YesOrNoOrNotKnown bailiffPartnerInARefuge;

    @CCD(label = "Do you know your partner's phone number?")
    private YesOrNo bailiffKnowPartnersPhone;

    @CCD(
        label = "Partners phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String bailiffPartnersPhone;

    @CCD(label = "Do you know your partner's date of birth?")
    private YesOrNo bailiffKnowPartnersDateOfBirth;

    @CCD(
        label = "What is your partner's date of birth?",
        typeOverride = Date
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bailiffPartnersDateOfBirth;

    @CCD(
        label = "What is your partner's approximate age?"
    )
    private Integer bailiffPartnersApproximateAge;

    @CCD(
        label = "How tall is your partner?"
    )
    private String bailiffPartnersHeight;

    @CCD(
        label = "What is your partner's hair colour?"
    )
    private String bailiffPartnersHairColour;

    @CCD(
        label = "What is your partner's eye colour?"
    )
    private String bailiffPartnersEyeColour;

    @CCD(
        label = "What is your partner's ethnic group?",
        typeOverride = TextArea
    )
    private String bailiffPartnersEthnicGroup;

    @CCD(
        label = "Does your partner have distinguishing features?",
        typeOverride = TextArea
    )
    private String bailiffPartnersDistinguishingFeatures;

    @CCD(label = "When is best to serve papers to your partner?")
    private String bailiffBestTimeToServePapers;

    @CCD(label = "Does your partner have access to a vehicle?")
    private YesOrNoOrNotKnown bailiffDoesPartnerHaveVehicle;

    @JsonUnwrapped(prefix = "bailiffPartnerVehicle")
    @CCD(label = "Partner vehicle details")
    private Vehicle bailiffPartnerVehicle;

    @CCD(label = "Has your partner been violent in the past?")
    private YesOrNoOrNotKnown bailiffHasPartnerBeenViolent;

    @CCD(
        label = "Provide details of any violent incidents",
        typeOverride = TextArea
    )
    private String bailiffPartnerViolenceDetails;

    @CCD(label = "Has your partner made threats against you?")
    private YesOrNoOrNotKnown bailiffHasPartnerMadeThreats;

    @CCD(
        label = "Provide details of any threats",
        typeOverride = TextArea
    )
    private String bailiffPartnerThreatsDetails;

    @CCD(label = "Have police been involved with your partner?")
    private YesOrNoOrNotKnown bailiffHavePoliceBeenInvolved;

    @CCD(
        label = "Provide details of any police involvement",
        typeOverride = TextArea
    )
    private String bailiffPoliceInvolvedDetails;

    @CCD(label = "Have social services been involved with your partner?")
    private YesOrNoOrNotKnown bailiffHaveSocialServicesBeenInvolved;

    @CCD(
        label = "Provide details of any social services involvement",
        typeOverride = TextArea
    )
    private String bailiffSocialServicesInvolvedDetails;

    @CCD(label = "Are there any dangerous animals at the property?")
    private YesOrNoOrNotKnown bailiffAreThereDangerousAnimals;

    @CCD(
        label = "Provide details of any dangerous animals",
        typeOverride = TextArea
    )
    private String bailiffDangerousAnimalsDetails;
}
