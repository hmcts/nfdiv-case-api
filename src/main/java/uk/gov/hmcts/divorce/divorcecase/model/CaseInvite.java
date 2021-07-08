package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseInvite {

    @CCD(
        label = "The respondent's email address",
        access = {DefaultAccess.class}
    )
    private String applicant2InviteEmailAddress;

    @CCD(
        label = "The respondent invite access code",
        access = {DefaultAccess.class}
    )
    private String accessCode;

    @CCD(
        label = "The respondent's user id",
        access = {DefaultAccess.class}
    )
    private String applicant2UserId;

}
