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

import java.time.LocalDateTime;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class ConditionalOrderQuestions {

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
        label = "Has applicant submitted conditional order"
    )
    private YesOrNo isSubmitted;

    @CCD(
        label = "Has applicant drafted conditional order"
    )
    private YesOrNo isDrafted;

    @CCD(
        label = "Date applicant submitted Conditional Order to HMCTS"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime submittedDate;

    @CCD(
        label = "Does the applicant want to continue with the divorce and apply for a conditional order?"
    )
    private YesOrNo applyForConditionalOrder;

    @CCD(
        label = "Is the information in this application still correct?"
    )
    private YesOrNo confirmInformationStillCorrect;

    @CCD(
        label = "Provide details of any other information that needs updating.",
        typeOverride = TextArea
    )
    private String reasonInformationNotCorrect;

    @CCD(
        label = "Has the applicant started the process to apply for conditional order?"
    )
    private YesOrNo applyForConditionalOrderStarted;

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
        label = "The applicant believes that the facts stated in the application for a conditional order are true."
    )
    private YesOrNo statementOfTruth;
}
