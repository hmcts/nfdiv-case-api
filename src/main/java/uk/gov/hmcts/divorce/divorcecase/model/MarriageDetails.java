package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class MarriageDetails {

    @CCD(
        label = "The applicant's full name as on ${labelContentMarriageOrCivilPartnership} certificate",
        hint = "Exactly as it appears on the certificate. Include any additional text such as 'formally known as'."
    )
    private String applicant1Name;

    @CCD(
        label = "${labelContentTheApplicant2UC} full name as on ${labelContentMarriageOrCivilPartnership} certificate",
        hint = "Exactly as it appears on the certificate. Include any additional text such as 'formally known as'."
    )
    private String applicant2Name;

    @CCD(
        label = "Did the ${labelContentMarriageOrCivilPartnership} take place in the UK?"
    )
    private YesOrNo marriedInUk;

    @CCD(
        label = "${labelContentMarriageOrCivilPartnershipUC} certificate in English?"
    )
    private YesOrNo certificateInEnglish;

    @CCD(
        label = "${labelContentMarriageOrCivilPartnershipUC} certificate translation"
    )
    private YesOrNo certifiedTranslation;

    @CCD(
        label = "Country of ${labelContentMarriageOrCivilPartnership}",
        hint = "Enter the country in which the ${labelContentMarriageOrCivilPartnership} took place"
    )
    private String countryOfMarriage;

    @CCD(
        label = "Place of ${labelContentMarriageOrCivilPartnership}",
        hint = "Enter the place of ${labelContentMarriageOrCivilPartnership} as it appears on the certificate"
    )
    private String placeOfMarriage;

    @CCD(
        label = "${labelContentMarriageOrCivilPartnershipUC} date",
        typeOverride = Date
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @CCD(
        label = "Were ${labelContentTheApplicantOrApplicant1} and ${labelContentTheApplicant2} a same-sex couple when "
            + "they ${labelContentGotMarriedOrFormedCivilPartnership}?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "MarriageFormation"
    )
    private MarriageFormation formationType;

    @CCD(
        label = "Is the ${labelContentMarriageOrCivilPartnership} certificate correct?"
    )
    private YesOrNo certifyMarriageCertificateIsCorrect;

    @CCD(
        label = "Why is the ${labelContentMarriageOrCivilPartnershipUC} certification incorrect",
        hint = "e.g. wrong names, misspellings, poor quality photo or scan, damaged, or missing entirely"
    )
    private String marriageCertificateIsIncorrectDetails;

    @CCD(
        label = "Issue application anyway?"
    )
    private YesOrNo issueApplicationWithoutMarriageCertificate;
}
