package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class AmendCase implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder<CaseData, UserRole, State> pageBuilder) {
        pageBuilder.page("amendCase")
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getDate)
                .done()
            .done()
            .complex(CaseData::getApplicant1)
                .optional(Applicant::getFirstName)
                .optional(Applicant::getMiddleName)
                .optional(Applicant::getLastName)
                .mandatory(Applicant::getHomeAddress)
                .optional(Applicant::getCorrespondenceAddress)
                .optional(Applicant::getPhoneNumber)
                .optional(Applicant::getEmail)
            .done()
            .complex(CaseData::getApplicant2)
                .optionalWithLabel(Applicant::getFirstName,
                    "Respondent's first name")
                .optionalWithLabel(Applicant::getMiddleName,
                    "Respondent's middle name(s)")
                .optionalWithLabel(Applicant::getLastName,
                    "Respondent's last name")
                .mandatoryWithLabel(Applicant::getHomeAddress,
                    "Respondent's home address")
                .optionalWithLabel(Applicant::getCorrespondenceAddress,
                    "Respondent's service address")
                .optionalWithLabel(Applicant::getEmail,
                    "Respondent's email address")
                .optionalWithLabel(Applicant::getPhoneNumber,
                    "Respondent's phone number")
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getPlaceOfMarriage)
                .done()
            .done();
    }
}
