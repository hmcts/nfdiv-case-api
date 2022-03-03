package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;

public class GeneralApplicationSelectApplicationType implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationSelectType")
            .complex(CaseData::getGeneralReferral)
                .mandatory(GeneralReferral::getGeneralApplicationType)
                .mandatory(GeneralReferral::getGeneralApplicationTypeOtherComments,
                    "generalApplicationType=\"Other\"")
                .done()
            .done();
    }
}
