package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class SolUpdateContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolUpdateContactDetails")
            .pageLabel("Update contact details")
            .mandatory(CaseData::getApplicant1SolicitorName)
            .mandatory(CaseData::getApplicant1SolicitorPhone)
            .mandatory(CaseData::getApplicant1SolicitorEmail)
            .mandatory(CaseData::getSolicitorAgreeToReceiveEmails)
            .mandatory(CaseData::getDerivedApplicant1SolicitorAddress)
            .done();
    }
}
