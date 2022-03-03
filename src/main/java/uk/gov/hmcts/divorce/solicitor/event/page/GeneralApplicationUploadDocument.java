package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;

public class GeneralApplicationUploadDocument implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationUploadDocument")
            .pageLabel("Upload document")
            .complex(CaseData::getGeneralReferral)
                .mandatory(GeneralReferral::getGeneralApplicationDocument)
                .optional(GeneralReferral::getGeneralApplicationDocumentComments)
                .done()
            .done();
    }
}
