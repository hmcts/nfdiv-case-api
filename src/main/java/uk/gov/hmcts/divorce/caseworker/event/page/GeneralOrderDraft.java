package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class GeneralOrderDraft implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder<CaseData, UserRole, State> pageBuilder) {
        pageBuilder.page("generalOrderDraft")
            .complex(CaseData::getGeneralOrder)
                .readonly(GeneralOrder::getGeneralOrderDraft)
                .done();
    }
}
