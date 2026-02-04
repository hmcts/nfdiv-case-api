package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Builder;
import uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator;

public record CaseInviteApp1(
    String applicant1InviteEmailAddress,
    String accessCodeApplicant1,
    String applicant1UserId) {

    @Builder()
    public CaseInviteApp1 {}

    public CaseInviteApp1 generateAccessCode() {
        return new CaseInviteApp1(applicant1InviteEmailAddress, AccessCodeGenerator.generateAccessCode(), applicant1UserId);
    }

    public CaseInviteApp1 useAccessCode() {
        return new CaseInviteApp1(applicant1InviteEmailAddress, null, applicant1UserId);
    }
}
