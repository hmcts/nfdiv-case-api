package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.ccd.sdk.types.CCD;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.Gender;

import static uk.gov.hmcts.ccd.sdk.types.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.types.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.types.FieldType.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode()
public class CaseData {

    @JsonProperty("divorceOrDissolution")
    @CCD(
        label = "Divorce or Dissolution?",
        type = FixedRadioList,
        typeParameter = "DivorceOrDissolution"
    )
    private DivorceOrDissolution divorceOrDissolution;

    @JsonProperty("D8ScreenHasMarriageBroken")
    @CCD(
        label = "Marriage broken irretrievably?",
        hint = "Has petitioner's marriage broken down irretrievably?",
        type = YesOrNo
    )
    private String d8ScreenHasMarriageBroken;

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
        typeParameter = "Gender"
    )
    private Gender d8InferredPetitionerGender;

    @JsonProperty("D8InferredRespondentGender")
    @CCD(
        label = "What is the respondent's gender?",
        hint = "The respondent’s gender is collected for statistical purposes only.",
        typeParameter = "Gender"
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
        label = "Need help with fees?",
        type = YesOrNo
    )
    private String d8HelpWithFeesNeedHelp;

    @JsonProperty("D8ScreenHasMarriageCert")
    @CCD(
        label = "Petitioner got marriage cert.?",
        type = YesOrNo
    )
    private String d8ScreenHasMarriageCert;

    @JsonProperty("D8HelpWithFeesAppliedForFees")
    @CCD(
        label = "Applied for help with fees?",
        type = YesOrNo
    )
    private String d8HelpWithFeesAppliedForFees;

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
        type = YesOrNo
    )
    private String d8CertifiedTranslation;
}
