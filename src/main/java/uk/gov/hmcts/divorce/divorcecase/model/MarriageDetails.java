package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class MarriageDetails {

    @CCD(
        label = "The applicant's full name as on marriage certificate",
        hint = "Enter the applicant's name exactly as it appears on the marriage certificate. "
            + " Include any extra text such as \"formerly known as\"",
        access = {DefaultAccess.class}
    )
    private String applicant1Name;

    @CCD(
        label = "${labelContentTheApplicant2UC} full name as on marriage certificate",
        hint = "Enter ${labelContentTheApplicant2}'s name exactly as it appears on the marriage certificate. "
            + " Include any extra text such as \"formerly known as\"",
        access = {DefaultAccess.class}
    )
    private String applicant2Name;

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
    private String countryOfMarriage;

    @CCD(
        label = "Place of marriage",
        hint = "Enter the place of marriage as it appears on the marriage certificate",
        access = {DefaultAccess.class}
    )
    private String placeOfMarriage;

    @CCD(
        label = "Marriage date",
        typeOverride = Date,
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @CCD(
        label = "Were the applicant and ${labelContentTheApplicant2} a same-sex couple when they got married?",
        access = {DefaultAccess.class}
    )
    private YesOrNo isSameSexCouple;

    @CCD(
        label = "Is the marriage certificate correct?",
        access = {DefaultAccess.class}
    )
    private YesOrNo certifyMarriageCertificateIsCorrect;

    @CCD(
        label = "Why is the Marriage certification incorrect",
        hint = "e.g. wrong names, misspellings, poor quality photo or scan, damaged, or missing entirely",
        access = {DefaultAccess.class}
    )
    private String marriageCertificateIsIncorrectDetails;

    @CCD(
        label = "Issue application anyway?",
        access = {DefaultAccess.class}
    )
    private YesOrNo issueApplicationWithoutMarriageCertificate;
}
