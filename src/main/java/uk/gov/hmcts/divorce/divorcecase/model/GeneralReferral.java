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
        label = "Does this case require an Urgent Referral?"
    )
    private YesOrNo generalReferralUrgentCase;

    @CCD(
        label = "Urgent referral reason",
        typeOverride = TextArea
    )
    private String generalReferralUrgentCaseReason;

    @CCD(
        label = "Is this a fraud referral?"
    )
    private YesOrNo generalReferralFraudCase;

    @CCD(
        label = "Fraud referral reason",
        typeOverride = TextArea
    )
    private String generalReferralFraudCaseReason;
}
