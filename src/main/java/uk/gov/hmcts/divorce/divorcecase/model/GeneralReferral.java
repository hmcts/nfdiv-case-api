package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class GeneralReferral {

    @CCD(
        label = "Reason for referral?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "GeneralReferralReason"
    )
    private GeneralReferralReason generalReferralReason;

    @CCD(
        label = "Application from",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralParties"
    )
    private GeneralParties generalApplicationFrom;

    @CCD(
        label = "Application or referral date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationReferralDate;

    @CCD(
        label = "General application added date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationAddedDate;

    @CCD(
        label = "Type of referral",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralReferralType"
    )
    private GeneralReferralType generalReferralType;

    @CCD(
        label = "Medium requested for alternative service",
        typeOverride = FixedList,
        typeParameterOverride = "AlternativeServiceMediumType"
    )
    private AlternativeServiceMediumType alternativeServiceMedium;

    @CCD(
        label = "Further details for Judge or Legal Advisor",
        typeOverride = TextArea
    )
    private String generalReferralJudgeOrLegalAdvisorDetails;

    @CCD(
        label = "Is fee payment required?"
    )
    private YesOrNo generalReferralFeeRequired;

    @JsonUnwrapped(prefix = "generalReferralFee")
    @Builder.Default
    private FeeDetails generalReferralFee = new FeeDetails();

    @CCD(
        label = "Approve general consideration?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "GeneralReferralDecision"
    )
    private GeneralReferralDecision generalReferralDecision;

    @CCD(
        label = "General referral decision date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate generalReferralDecisionDate;

    @CCD(
        label = "Please provide further details",
        hint = "Provide direction for any general orders or general letters to be created by caseworkers.",
        typeOverride = TextArea
    )
    private String generalReferralDecisionReason;

    @CCD(
        label = "Urgent general referral case?"
    )
    private YesOrNo generalReferralUrgentCase;

    @CCD(
        label = "Choose General Application Type"
    )
    private GeneralApplicationType generalApplicationType;

    @CCD(
        label = "Please provide more information about general application type",
        typeOverride = TextArea
    )
    private String generalApplicationTypeOtherComments;

    @CCD(
        label = "Choose General Application Fee Type"
    )
    private GeneralApplicationFee generalApplicationFeeType;

    @CCD(
        label = "General Application Document"
    )
    private DivorceDocument generalApplicationDocument;

    @CCD(
        label = "Additional comments about the supporting document",
        typeOverride = TextArea
    )
    private String generalApplicationDocumentComments;

    @JsonUnwrapped(prefix = "generalApplicationFee")
    @Builder.Default
    private FeeDetails generalApplicationFee = new FeeDetails();
}
