package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;

public class AmendCase implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("amendCase")
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getDate)
                .done()
            .done()
            .readonlyNoSummary(CaseData::getApplicationType,
                "applicant1FirstName=\"NeverShow\"")
            .complex(CaseData::getApplicant1)
                .optional(Applicant::getFirstName,
                    JOINT_APPLICATION_CONDITION)
                .optional(Applicant::getMiddleName,
                    JOINT_APPLICATION_CONDITION)
                .optional(Applicant::getLastName,
                    JOINT_APPLICATION_CONDITION)
                .optional(Applicant::getHomeAddress,
                    JOINT_APPLICATION_CONDITION)
                .optional(Applicant::getCorrespondenceAddress,
                    JOINT_APPLICATION_CONDITION)
                .optional(Applicant::getPhoneNumber,
                    JOINT_APPLICATION_CONDITION)
                .optional(Applicant::getEmail,
                    JOINT_APPLICATION_CONDITION)
            .done()
            .complex(CaseData::getApplicant2)
                .optionalWithoutDefaultValue(Applicant::getFirstName,
                    JOINT_APPLICATION_CONDITION,
                    "Respondent's first name")
                .optionalWithoutDefaultValue(Applicant::getMiddleName,
                    JOINT_APPLICATION_CONDITION,
                    "Respondent's middle name(s)")
                .optionalWithoutDefaultValue(Applicant::getLastName,
                    JOINT_APPLICATION_CONDITION,
                    "Respondent's last name")
                .optionalWithoutDefaultValue(Applicant::getHomeAddress,
                    JOINT_APPLICATION_CONDITION,
                    "Respondent's home address")
                .optionalWithoutDefaultValue(Applicant::getCorrespondenceAddress,
                    JOINT_APPLICATION_CONDITION,
                    "Respondent's service address")
                .optionalWithoutDefaultValue(Applicant::getEmail,
                    JOINT_APPLICATION_CONDITION,
                    "Respondent's email address")
                .optionalWithoutDefaultValue(Applicant::getPhoneNumber,
                    JOINT_APPLICATION_CONDITION,
                    "Respondent's phone number")
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getPlaceOfMarriage)
                .done()
            .done();
    }
}
