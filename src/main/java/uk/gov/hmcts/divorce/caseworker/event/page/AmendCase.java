package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

public class AmendCase implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("amendCase")
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getDate)
                    .optional(MarriageDetails::getPlaceOfMarriage)
                .done()
            .done();
    }
}
