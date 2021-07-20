package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;

public class AmendCase implements CcdPageConfiguration {
    private static final String NO_DEFAULT_VALUE = null;

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
                .optional(Applicant::getFirstName,
                    JOINT_APPLICATION_CONDITION,
                    NO_DEFAULT_VALUE,
                    "Respondent's first name")
                .optional(Applicant::getMiddleName,
                    JOINT_APPLICATION_CONDITION,
                    NO_DEFAULT_VALUE,
                "Respondent's middle name(s)")
                .optional(Applicant::getLastName,
                    JOINT_APPLICATION_CONDITION,
                    NO_DEFAULT_VALUE,
                    "Respondent's last name")
                .optional(Applicant::getHomeAddress,
                    JOINT_APPLICATION_CONDITION,
                    NO_DEFAULT_VALUE,
                    "Respondent's home address")
                .optional(Applicant::getCorrespondenceAddress,
                    JOINT_APPLICATION_CONDITION,
                    NO_DEFAULT_VALUE,
                    "Respondent's service address")
                .optional(Applicant::getEmail,
                    JOINT_APPLICATION_CONDITION,
                    NO_DEFAULT_VALUE,
                    "Respondent's email address")
                .optional(Applicant::getPhoneNumber,
                    JOINT_APPLICATION_CONDITION,
                    NO_DEFAULT_VALUE,
                    "Respondent's phone number")
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getPlaceOfMarriage)
                .done()
            .done();
    }
}
