package uk.gov.hmcts.divorce.solicitor.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

@Component
public class UpdateApplicant2ContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2ContactDetails")
            .pageLabel("Update contact details")
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getEmail,
                    "${labelContentTheApplicant2UC} email address")
                .mandatoryWithLabel(Applicant::getPhoneNumber,
                    "${labelContentTheApplicant2UC} phone number")
                .optionalWithLabel(Applicant::getAddressOverseas, "Is ${labelContentTheApplicant2UC} address international?")
                .mandatoryWithLabel(Applicant::getAddress,
                    "${labelContentTheApplicant2UC} home address")
                .label("LabelHorizontalLine1App2", "<hr>")
                .mandatoryWithLabel(Applicant::getContactDetailsType,
                    "Should ${labelContentTheApplicant2} contact details be kept private?")
                .done();
    }
}
