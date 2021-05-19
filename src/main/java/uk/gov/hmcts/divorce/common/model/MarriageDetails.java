package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class MarriageDetails {

    @CCD(
        label = "Did the marriage take place in the UK?"
    )
    private YesOrNo marriedInUk;

    @CCD(
        label = "Marriage certificate in English?"
    )
    private YesOrNo certificateInEnglish;

    @CCD(
        label = "Marriage certificate translation"
    )
    private YesOrNo certifiedTranslation;

    @CCD(
        label = "Country of marriage",
        hint = "Enter the country in which the marriage took place"
    )
    private String countryOfMarriage;

    @CCD(
        label = "Place of marriage",
        hint = "Enter the place of marriage as it appears on the marriage certificate"
    )
    private String placeOfMarriage;

    @CCD(
        label = "Marriage date",
        typeOverride = Date
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @CCD(
        label = "Were applicant 1 and applicant 2 a same-sex couple when they got married?",
        access = {DefaultAccess.class}
    )
    private YesOrNo isSameSexCouple;

}
