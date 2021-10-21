package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class ConditionalOrderNewDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("ConditionalOrderNewDocuments")
            .pageLabel("Documents - Draft Conditional Order Application")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getAddNewDocuments)
                .mandatory(ConditionalOrder::getDocumentsUploaded, "coAddNewDocuments=\"Yes\"")
            .done();
    }
}
