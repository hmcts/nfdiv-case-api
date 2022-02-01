package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator;

import static java.util.Objects.isNull;

public record CaseInvite(
    @CCD(
        label = "The respondent's email address",
        access = {DefaultAccess.class}
    )
    String applicant2InviteEmailAddress,

    @CCD(
        label = "The respondent invite access code",
        access = {DefaultAccess.class}
    )
    String accessCode,

    @CCD(
        label = "The respondent's user id",
        access = {DefaultAccess.class}
    )
    String applicant2UserId) {

    @Builder()
    public CaseInvite {}

    @JsonIgnore
    public boolean isApplicant2(String userId) {
        return !isNull(applicant2UserId) && userId.equals(applicant2UserId);
    }

    @JsonIgnore
    public CaseInvite generateAccessCode() {
        return new CaseInvite(applicant2InviteEmailAddress, AccessCodeGenerator.generateAccessCode(), applicant2UserId);
    }

    public CaseInvite useAccessCode() {
        return new CaseInvite(applicant2InviteEmailAddress, null, applicant2UserId);
    }
}
