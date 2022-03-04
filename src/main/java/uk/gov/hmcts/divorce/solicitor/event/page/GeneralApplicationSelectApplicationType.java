package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public class GeneralApplicationSelectApplicationType implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationSelectType")
            .complex(CaseData::getGeneralApplication)
                .mandatory(GeneralApplication::getType)
                .mandatory(GeneralApplication::getTypeOtherComments,
                    "generalApplicationType=\"Other\"")
                .done()
            .done();
    }
}
