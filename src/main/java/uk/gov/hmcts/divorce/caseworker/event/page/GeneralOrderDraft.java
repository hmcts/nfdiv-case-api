package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;

public class GeneralOrderDraft implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("generalOrderDraft")
            .complex(CaseData::getGeneralOrder)
                .readonly(GeneralOrder::getGeneralOrderUseScannedDraft, "generalOrderScannedDraft=\"NEVER_SHOW\"")
                .readonly(GeneralOrder::getGeneralOrderDraft, "generalOrderUseScannedDraft=\"No\"")
                .readonly(GeneralOrder::getGeneralOrderScannedDraft, "generalOrderUseScannedDraft=\"Yes\"")
            .done();
    }
}
