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

    @CCD(label = "The applicant's or applicant 1’s")
    private String applicantOrApplicant1;

    @CCD(label = "Divorce or civil partnership")
    private String divorceOrCivilPartnership;

    @CCD(label = "Divorce or civil partnership")
    private String finaliseDivorceOrEndCivilPartnership;

    @CCD(label = "marriage or civil partnership")
    private String marriageOrCivilPartnership;

    @CCD(label = "Marriage or civil partnership")
    private String marriageOrCivilPartnershipUC;

    @CCD(label = "Get a divorce or legally end it")
    private String divorceOrLegallyEnd;

    @CCD(label = "applicant's or applicant 1’s")
    private String applicantsOrApplicant1s;

    @CCD(label = "the applicant or applicant 1")
    private String theApplicantOrApplicant1;

    @CCD(label = "The applicant or applicant 1")
    private String theApplicantOrApplicant1UC;

    @CCD(label = "Applicant or Applicant 1")
    private String applicantOrApplicant1UC;

    @CCD(label = "Got married or formed their civil partnership")
    private String gotMarriedOrFormedCivilPartnership;

    @CCD(label = "respondent's or applicant 2’s")
    private String respondentsOrApplicant2s;

    @CCD(label = "divorce or ending of your civil partnership")
    private String divorceOrEndingCivilPartnership;

    @CCD(label = "finalise divorce or legally end your civil partnership")
    private String finaliseDivorceOrLegallyEndYourCivilPartnership;

    public void setUnionType(DivorceOrDissolution divorceOrDissolution) {
        if (divorceOrDissolution != null && divorceOrDissolution.isDivorce()) {
            unionType = "divorce";
            unionTypeUC = "Divorce";
            divorceOrCivilPartnershipApplication = "divorce application";
            divorceOrEndCivilPartnership = "for divorce";
            divorceOrCivilPartnership = "divorce";
            finaliseDivorceOrEndCivilPartnership = "finalise the divorce";
            marriageOrCivilPartnership = "marriage";
            marriageOrCivilPartnershipUC = "Marriage";
            divorceOrLegallyEnd = "get a divorce";
            gotMarriedOrFormedCivilPartnership = "got married";
            divorceOrEndingCivilPartnership = "divorce";
            finaliseDivorceOrLegallyEndYourCivilPartnership = "finalise the divorce";
        } else {
            unionType = "dissolution";
            unionTypeUC = "Dissolution";
            divorceOrCivilPartnershipApplication = "application to end the civil partnership";
            divorceOrEndCivilPartnership = "to end the civil partnership";
            divorceOrCivilPartnership = "civil partnership";
            finaliseDivorceOrEndCivilPartnership = "end civil partnership";
            marriageOrCivilPartnership = "civil partnership";
            marriageOrCivilPartnershipUC = "Civil partnership";
            divorceOrLegallyEnd = "legally end it";
            gotMarriedOrFormedCivilPartnership = "formed their civil partnership";
            divorceOrEndingCivilPartnership = "ending of your civil partnership";
            finaliseDivorceOrLegallyEndYourCivilPartnership = "legally end your civil partnership";
        }
    }

    public void setApplicationType(ApplicationType applicationType) {
        if (applicationType != null && applicationType.isSole()) {
            applicant2 = "respondent";
            theApplicant2 = "the respondent";
            applicant2UC = "Respondent";
            theApplicant2UC = "The respondent";
            applicantOrApplicant1 = "the applicant’s";
            applicantsOrApplicant1s = "Applicant’s";
            theApplicantOrApplicant1 = "the applicant";
            theApplicantOrApplicant1UC = "The applicant";
            respondentsOrApplicant2s = "Respondent's";
            applicantOrApplicant1UC = "Applicant";
        } else {
            applicant2 = "applicant 2";
            theApplicant2 = "applicant 2";
            applicant2UC = "Applicant 2";
            theApplicant2UC = "Applicant 2";
            applicantOrApplicant1 = "applicant 1’s";
            applicantsOrApplicant1s = "Applicant 1’s";
            theApplicantOrApplicant1 = "applicant 1";
            theApplicantOrApplicant1UC = "Applicant 1";
            respondentsOrApplicant2s = "Applicant 2's";
            applicantOrApplicant1UC = "Applicant 1";
        }
    }
}
