package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class Applicant1UpdateContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("ApplicantContactDetails")
            .pageLabel("Update applicants contact details")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getEmail)
                .mandatory(Applicant::getPhoneNumber)
                .mandatory(Applicant::getHomeAddress)
                .label("LabelHorizontalLine1", "<hr>")
                .mandatory(Applicant::getContactDetailsConfidential)
                .done();
    }
}
