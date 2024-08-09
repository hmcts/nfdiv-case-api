package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public class GeneralApplicationSelectApplicationType implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationSelectType")
            .pageLabel("Select Application Type")
            .complex(CaseData::getGeneralApplication)
                .mandatory(GeneralApplication::getGeneralApplicationType)
                .mandatory(GeneralApplication::getGeneralApplicationTypeOtherComments,
                    "generalApplicationType=\"other\"")
                .mandatory(GeneralApplication::getGeneralApplicationUrgentCase)
                .mandatory(GeneralApplication::getGeneralApplicationUrgentCaseReason, "generalApplicationUrgentCase=\"Yes\"")
                .done();
    }
}
