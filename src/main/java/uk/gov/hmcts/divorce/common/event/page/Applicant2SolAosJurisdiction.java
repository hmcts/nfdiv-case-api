package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;

public class Applicant2SolAosJurisdiction implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolAosJurisdiction")
            .pageLabel("The jurisdiction of the courts")
            .complex(CaseData::getApplication)
            .complex(Application::getJurisdiction)
                .readonlyWithLabel(Jurisdiction::getConnections,
                    "The applicant indicated the courts of England and Wales have jurisdiction because:")
            .done()
            .done()
            .complex(CaseData::getAcknowledgementOfService)
            .mandatoryWithLabel(AcknowledgementOfService::getJurisdictionAgree,
                "Does the respondent agree the courts of England and Wales have jurisdiction?")
            .mandatory(AcknowledgementOfService::getReasonCourtsOfEnglandAndWalesHaveNoJurisdiction, "jurisdictionAgree=\"No\"")
            .mandatory(AcknowledgementOfService::getInWhichCountryIsYourLifeMainlyBased, "jurisdictionAgree=\"No\"")
            .done();
    }
}
