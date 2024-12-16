package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.divorce.divorcecase.model.access.InternalCaseFlagsAccess;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyFlags {
    @CCD(access = {InternalCaseFlagsAccess.class},
        label = "Applicant/Applicant1 Flags")
    private Flags applicant1Flags;

    @CCD(access = {InternalCaseFlagsAccess.class},
        label = "Respondent/Applicant2 Flags")
    private Flags applicant2Flags;

    @CCD(access = {InternalCaseFlagsAccess.class},
        label = "Applicant/Applicant1 Solicitor Flags")
    private Flags applicant1SolicitorFlags;

    @CCD(access = {InternalCaseFlagsAccess.class},
        label = "Respondent/Applicant2 Solicitor Flags")
    private Flags applicant2SolicitorFlags;

    /* To support external flags in the future, new flags for parties can be added here.
        Same group id should be used for internal and external flags for a particular case party.
     */

    private String applicant1GroupId;
    private String applicant2GroupId;
    private String applicant1SolicitorGroupId;
    private String applicant2SolicitorGroupId;
}
