package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;

public class JurisdictionApplyForDivorce implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("JurisdictionApplyForDivorce")
            .pageLabel("Jurisdiction - Apply for a divorce")
            .label(
                "LabelSolJurisdictionPara-1",
                "The court has legal power to deal with this application because the following applies:\n\n"
                    + "Divorce – *Opposite Sex Couple* – Article 3(1) of Council Regulation (EC) No 2201/2003"
                    + "of 27 November 2003")
            .complex(CaseData::getApplication)
                .complex(Application::getJurisdiction)
                    .mandatory(Jurisdiction::getLegalConnections)
                    .done()
                .done();
    }
}
