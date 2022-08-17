package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SwitchedToSole {

    @CCD(
        label = "Is the respondent's email address known?"
    )
    private YesOrNo applicant1KnowsApplicant2EmailAddress;

    @CCD(
        label = "Is the respondent's home address known?"
    )
    private YesOrNo applicant1KnowsApplicant2Address;

    @CCD(
        label = "Applicant 2 is using digital channel?"
    )
    private YesOrNo app2ContactMethodIsDigital;

    @CCD(
        label = "Want to apply to have the papers 'served' (sent to respondent) in another way",
        hint = "For example by email, text message or social media. This is a separate application with "
            + "an additional fee, which will need to be reviewed by a judge."
    )
    private YesOrNo applicant1WantsToHavePapersServedAnotherWay;

    @CCD(
        label = "Is the information provided on the case correct?"
    )
    private YesOrNo applicant2ConfirmApplicant1Information;

    @CCD(
        label = "Explain what is incorrect or needs changing.",
        typeOverride = TextArea
    )
    private String applicant2ExplainsApplicant1IncorrectInformation;

    @CCD(
        label = "Does ${labelContentTheApplicant2} have a solicitor representing them?"
    )
    private Applicant2Represented applicant1IsApplicant2Represented;

    @CCD(
        label = "The respondent agrees that the divorce service can send notifications by email."
    )
    private YesOrNo applicant2AgreeToReceiveEmails;

    @CCD(
        label = "Does Applicant 2 need help with fees?"
    )
    private YesOrNo applicant2NeedsHelpWithFees;

    @CCD(
        label = "I have discussed the possibility of a reconciliation with the applicant."
    )
    private YesOrNo solStatementOfReconciliationCertify;

    @CCD(
        label = "I have given the applicant the names and addresses of persons qualified to help effect a reconciliation."
    )
    private YesOrNo solStatementOfReconciliationDiscussed;
}
