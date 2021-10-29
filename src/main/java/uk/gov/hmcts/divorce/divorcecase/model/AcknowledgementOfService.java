package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.AosAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDateTime;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcknowledgementOfService {

    @CCD(
        label = "Has the respondent read the application for divorce?",
        access = AosAccess.class
    )
    private YesOrNo confirmReadPetition;

    @CCD(
        label = "Respondent wants to dispute the application",
        access = AosAccess.class
    )
    private YesOrNo disputeApplication;

    @CCD(
        label = "Respondent confirms that they want to dispute the application",
        access = AosAccess.class
    )
    private YesOrNo confirmDisputeApplication;

    @CCD(
        label = "Respondent agreed to claimed jurisdiction?",
        access = AosAccess.class
    )
    private YesOrNo jurisdictionAgree;

    @CCD(
        label = "Reason respondent disagreed to claimed jurisdiction",
        typeOverride = TextArea,
        access = AosAccess.class
    )
    private String jurisdictionDisagreeReason;

    @CCD(
        label = "Date AoS submitted to HMCTS",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateAosSubmitted;

    @CCD(
        label = "Digital Notice of Proceedings?",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private YesOrNo digitalNoticeOfProceedings;

    @CCD(
        label = "Notice of Proceedings email address",
        typeOverride = Email,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String noticeOfProceedingsEmail;

    @CCD(
        label = "Notice of Proceedings solicitor's firm"
    )
    private String noticeOfProceedingsSolicitorFirm;

    @CCD(
        label = "No jurisdiction reason",
        access = {AosAccess.class}
    )
    private String reasonCourtsOfEnglandAndWalesHaveNoJurisdiction;

    @CCD(
        label = "Which country are you mainly based",
        access = {AosAccess.class}
    )
    private String inWhichCountryIsYourLifeMainlyBased;

    @CCD(
        label = "I am duly authorised by the respondent to sign this statement.",
        access = AosAccess.class
    )
    private YesOrNo statementOfTruth;

    @CCD(
        label = "The respondent has given their \"prayer\".",
        hint = "\"The prayer\" means they confirm they wish to dissolve the union, pay any fees (if applicable),"
            + " and have decided how money and property will be split (\"financial order\").",
        access = AosAccess.class
    )
    private YesOrNo prayerHasBeenGiven;

    @JsonIgnore
    public void setNoticeOfProceedings(final Solicitor solicitor) {
        digitalNoticeOfProceedings = YES;
        noticeOfProceedingsEmail = solicitor.getEmail();
        noticeOfProceedingsSolicitorFirm = solicitor.getOrganisationPolicy().getOrganisation().getOrganisationName();
    }
}
