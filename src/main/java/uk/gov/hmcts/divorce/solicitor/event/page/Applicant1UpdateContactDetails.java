package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class Applicant1UpdateContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("ApplicantContactDetails")
            .pageLabel("Update applicants contact details")
            .mandatory(CaseData::getApplicant1Email)
            .mandatory(CaseData::getApplicant1PhoneNumber)
            .mandatory(CaseData::getApplicant1HomeAddress)
            .label("LabelHorizontalLine1", "<hr>")
            .mandatory(CaseData::getApplicant1ContactDetailsConfidential);
    }
}
