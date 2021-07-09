package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class Applicant2SolAosjurisdiction implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolAosjurisdiction")
            .pageLabel("Do you agree that the courts of England and Wales have jurisdiction?")
            .complex(CaseData::getAcknowledgementOfService)
            .mandatory(AcknowledgementOfService::getJurisdictionAgree)
            .mandatory(AcknowledgementOfService::getJurisdictionDisagreeReason, "jurisdictionAgree=\"No\"")
            .done();
    }
}
