package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator;

import static java.util.Objects.isNull;

public record CaseInvite(
    String applicant2InviteEmailAddress,
    String accessCode,
    String applicant2UserId) {

    @Builder()
    public CaseInvite {}

    @JsonIgnore
    public boolean isApplicant2(String userId) {
        return !isNull(applicant2UserId) && userId.equals(applicant2UserId);
    }

    public CaseInvite generateAccessCode() {
        return new CaseInvite(applicant2InviteEmailAddress, AccessCodeGenerator.generateAccessCode(), applicant2UserId);
    }

    public CaseInvite useAccessCode() {
        return new CaseInvite(applicant2InviteEmailAddress, null, applicant2UserId);
    }
}
