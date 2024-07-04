package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;

public class GeneralApplicationSelectUrgentCase implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationSelectUrgentCase")
            .pageLabel("Select Application Urgency")
            .complex(CaseData::getGeneralApplication)
            .mandatory(GeneralApplication::getGeneralApplicationUrgentCase)
            .mandatory(GeneralApplication::getGeneralApplicationUrgentCaseReason, "generalApplicationUrgentCase=\"Yes\"")
            .done();
    }
}
