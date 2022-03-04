package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public class GeneralApplicationUploadDocument implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationUploadDocument")
            .pageLabel("Upload document")
            .complex(CaseData::getGeneralApplication)
                .mandatory(GeneralApplication::getDocument)
                .optional(GeneralApplication::getDocumentComments)
                .done()
            .done();
    }
}
