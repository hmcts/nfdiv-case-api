package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class LabelContent {

    @CCD(label = "applicant 2 or respondent")
    private String applicant2;

    @CCD(label = "applicant 2 or the respondent")
    private String theApplicant2;

    @CCD(label = "Applicant 2 or The respondent")
    private String theApplicant2UC;

    @CCD(label = "Applicant or Respondent")
    private String applicant2UC;

    @CCD(label = "divorce or dissolution")
    private String unionType;

    @CCD(label = "Divorce or Dissolution")
    private String unionTypeUC;

    @CCD(label = "Divorce or civil partnership application")
    private String divorceOrCivilPartnershipApplication;

    @CCD(label = "Divorce or end civil partnership")
    private String divorceOrEndCivilPartnership;

    public void setUnionType(DivorceOrDissolution divorceOrDissolution) {
        if (divorceOrDissolution != null && divorceOrDissolution.isDivorce()) {
            unionType = "divorce";
            unionTypeUC = "Divorce";
            divorceOrCivilPartnershipApplication = "divorce application";
            divorceOrEndCivilPartnership = "for divorce";
        } else {
            unionType = "dissolution";
            unionTypeUC = "Dissolution";
            divorceOrCivilPartnershipApplication = "application to end the civil partnership";
            divorceOrEndCivilPartnership = "to end the civil partnership";
        }
    }

    public void setApplicationType(ApplicationType applicationType) {
        if (applicationType != null && applicationType.isSole()) {
            applicant2 = "respondent";
            theApplicant2 = "the respondent";
            applicant2UC = "Respondent";
            theApplicant2UC = "The respondent";
        } else {
            applicant2 = "applicant 2";
            theApplicant2 = "applicant 2";
            applicant2UC = "Applicant 2";
            theApplicant2UC = "Applicant 2";
        }
    }
}
