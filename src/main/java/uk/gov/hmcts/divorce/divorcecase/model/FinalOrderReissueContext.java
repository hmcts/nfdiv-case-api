package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalOrderReissueContext {

    @CCD(label = "Original application type for FO reissue")
    private ApplicationType originalApplicationTypeForReissue;

    @CCD(label = "Original Applicant 1 full name for FO reissue")
    private String originalApplicant1FullNameForReissue;

    @CCD(label = "Original Applicant 2 full name for FO reissue")
    private String originalApplicant2FullNameForReissue;
}
