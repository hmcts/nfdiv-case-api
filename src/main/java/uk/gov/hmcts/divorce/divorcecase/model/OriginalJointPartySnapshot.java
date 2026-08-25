package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OriginalJointPartySnapshot {
    @CCD(label = "Original application type")
    private ApplicationType originalApplicationType;

    @CCD(label = "Original Applicant 1 full name")
    private String originalApplicant1FullName;

    @CCD(label = "Original Applicant 2 full name")
    private String originalApplicant2FullName;

    @CCD(label = "Snapshot captured date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate capturedDate;
}
