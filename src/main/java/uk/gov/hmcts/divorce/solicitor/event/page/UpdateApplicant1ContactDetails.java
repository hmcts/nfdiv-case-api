package uk.gov.hmcts.divorce.solicitor.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

@Component
public class UpdateApplicant1ContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("ApplicantContactDetails")
            .pageLabel("Update applicants contact details")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getEmail)
                .mandatory(Applicant::getPhoneNumber)
                .optional(Applicant::getAddressOverseas)
                .mandatory(Applicant::getAddress)
                .label("LabelHorizontalLine1", "<hr>")
                .mandatory(Applicant::getContactDetailsType)
                .done();
    }
}
