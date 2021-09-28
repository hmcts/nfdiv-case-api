package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;

public class ConditionalOrderNewDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("ConditionalOrderNewDocuments")
            .pageLabel("Documents - Draft Conditional Order Application")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getAddNewDocuments)
                .mandatory(ConditionalOrder::getDnDocumentsUploaded, "conOrderAddNewDocuments=\"Yes\"")
            .done();
    }
}
