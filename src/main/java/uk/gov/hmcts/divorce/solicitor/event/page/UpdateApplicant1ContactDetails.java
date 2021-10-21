package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class UpdateApplicant1ContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder) {
        pageBuilder
            .page("ApplicantContactDetails")
            .pageLabel("Update applicants contact details")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getEmail)
                .mandatory(Applicant::getPhoneNumber)
                .mandatory(Applicant::getHomeAddress)
                .label("LabelHorizontalLine1", "<hr>")
                .mandatory(Applicant::getKeepContactDetailsConfidential)
                .done();
    }
}
