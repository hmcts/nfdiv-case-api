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
            .mandatory(CaseData::getPetitionerSolicitorName)
            .mandatory(CaseData::getPetitionerSolicitorPhone)
            .mandatory(CaseData::getPetitionerSolicitorEmail)
            .mandatory(CaseData::getSolicitorAgreeToReceiveEmails)
            .mandatory(CaseData::getDerivedPetitionerSolicitorAddress)
            .done();
    }
}
