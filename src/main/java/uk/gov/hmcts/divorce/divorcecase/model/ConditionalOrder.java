package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class ConditionalOrder {

    @CCD(
        label = "Date Conditional Order submitted to HMCTS"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateSubmitted;

    @CCD(
        label = "Link to respondent answers"
    )
    private Document respondentAnswersLink;

    @CCD(
        label = "Does the applicant want to continue with the divorce and apply for a conditional order?"
    )
    private YesOrNo applyForConditionalOrder;

    @CCD(
        label = "Link to online petition"
    )
    private Document onlinePetitionLink;

    @CCD(
        label = "Do you need to change your application or add anything?",
        hint = "If you change or add anything which means your application has to be sent to your "
            + "husband/wife again you may have to pay a £95 fee"
    )
    private YesOrNo changeOrAddToApplication;

    @CCD(
        label = "Is everything stated in this divorce application true?"
    )
    private YesOrNo isEverythingInApplicationTrue;

    @CCD(
        label = "Solicitor’s name"
    )
    private String solicitorName;

    @CCD(
        label = "Solicitor’s firm"
    )
    private String solicitorFirm;

    @CCD(
        label = "Additional comments",
        typeOverride = TextArea
    )
    private String solicitorAdditionalComments;

    @CCD(
        label = "Grant Conditional Order?"
    )
    private YesOrNo granted;

    @CCD(
        label = "Grant Cost Order?"
    )
    private YesOrNo claimsGranted;

    @CCD(
        label = "Make costs order information",
        typeOverride = TextArea
    )
    private String claimsCostsOrderInformation;

    @CCD(
        label = "Legal advisor decision date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate decisionDate;

    @CCD(
        label = "Conditional Order granted date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate grantedDate;

    @CCD(
        label = "Refusal decision",
        typeOverride = FixedRadioList,
        typeParameterOverride = "RefusalOption"
    )
    private RefusalOption refusalDecision;

    @CCD(
        label = "Additional info",
        typeOverride = TextArea
    )
    private String refusalAdminErrorInfo;

    @CCD(
        label = "Refusal rejection reasons",
        typeOverride = MultiSelectList,
        typeParameterOverride = "RejectionReason"
    )
    private RejectionReason refusalRejectionReason;

    @CCD(
        label = "Rejection additional information",
        typeOverride = TextArea
    )
    private String refusalRejectionAdditionalInfo;

    @CCD(
        label = "Provide a refusal reason",
        hint = "Choose at least one of the following",
        typeOverride = MultiSelectList,
        typeParameterOverride = "ClarificationReason"
    )
    private ClarificationReason refusalClarificationReason;

    @CCD(
        label = "Clarification additional information (Translated)",
        typeOverride = TextArea
    )
    private String refusalClarificationAdditionalInfo;

    @CCD(
        label = "List of responses for Conditional Order clarification",
        typeOverride = TextArea
    )
    private String clarificationResponse;

    @CCD(
        label = "Upload any other documents per Clarification?",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> clarificationUploadDocuments;

    @CCD(
        label = "Case on digital Conditional Order Outcome"
    )
    private YesOrNo outcomeCase;

    @CCD(
        label = "Court name"
    )
    private ConditionalOrderCourt court;

    @CCD(
        label = "Date and time of hearing"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateAndTimeOfHearing;

    @CCD(
        label = "Pronouncement Judge"
    )
    private String pronouncementJudge;

    @CCD(
        label = "Grant Cost Order?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "JudgeCostsClaimGranted"
    )
    private JudgeCostsClaimGranted judgeCostsClaimGranted;

    @CCD(
        label = "Additional info",
        typeOverride = TextArea
    )
    private String judgeCostsOrderAdditionalInfo;

    @CCD(
        label = "Link to certificate of entitlement"
    )
    private DivorceDocument certificateOfEntitlementDocument;

    @CCD(
        label = "The applicant believes that the facts stated in this application are true.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantStatementOfTruth;
}
